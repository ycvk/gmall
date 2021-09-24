package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/22/9:00
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //异步编排方式
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            goods.setTitle(skuInfo.getSkuName());
            goods.setCreateTime(new Date());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
            goods.setId(skuId);
            return skuInfo;
        }, threadPoolExecutor);
        //获取分类数据
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Name(categoryView.getCategory3Name());
        }, threadPoolExecutor);

        //获取品牌数据
        CompletableFuture<Void> baseTrademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            goods.setTmId(trademark.getId());
            goods.setTmLogoUrl(trademark.getLogoUrl());
            goods.setTmName(trademark.getTmName());
        }, threadPoolExecutor);

        //获取平台属性
        CompletableFuture<Void> baseAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            List<SearchAttr> collect = attrList.stream().map(baseAttrInfo -> {
                //给searchAttr赋值
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                searchAttr.setAttrId(baseAttrInfo.getId());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(collect);
        }, threadPoolExecutor);

        //多任务组合
        CompletableFuture.allOf(skuInfoCompletableFuture,
                categoryViewCompletableFuture,
                baseAttrCompletableFuture,
                baseTrademarkCompletableFuture).join();
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {

        String key = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        //判断并重新赋值存入缓存
        if (score % 10 == 0) {
            Optional<Goods> goodsOptional = goodsRepository.findById(skuId);
            Goods goods = goodsOptional.get();
            goods.setHotScore(score.longValue());
            goodsRepository.save(goods);
        }
    }

    @SneakyThrows
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        //生成dsl语句
        SearchRequest searchRequest = buildQueryDsl(searchParam);
        //执行dsl语句
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //将searchResponse 转化为 SearchResponseVo
        SearchResponseVo searchResponseVo = parseSearchResponseVo(searchResponse);

        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //分页公式：((allCount-1)/pageSize)+1
        long totalPages = ((searchResponseVo.getTotal() - 1) / searchParam.getPageSize()) + 1;
        searchResponseVo.setTotalPages(totalPages);


        return searchResponseVo;
    }

    private SearchRequest buildQueryDsl(SearchParam searchParam) {
        //声明一个查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //query - bool - filter - term
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //判断是否根据3级分类Id进行搜索
        if (searchParam.getCategory1Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        if (searchParam.getCategory2Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (searchParam.getCategory3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }

        //根据关键字进行检索
        //query - bool -must - match
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
        }

        //根据品牌Id进行过滤
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            //  数据类似  1：小米
            String[] split = trademark.split(":");
            if (split.length == 2) {
                //spilt[0]是 tmId
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //根据平台属性值过滤
        String[] props = searchParam.getProps();
        //        for (String prop : props) {
//            String[] split = prop.split(":");
//            if (split.length == 3) {
//                //因为这个bool中还嵌入了2个boolquery，所以声明2个bool
//                BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
//                BoolQueryBuilder boolQueryBuilder2 = QueryBuilders.boolQuery();
//                //最里面的bool
//                //赋值Id
//                boolQueryBuilder2.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
//                //赋值平台属性名
//                boolQueryBuilder2.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
//                //bool - nested
//                boolQueryBuilder1.must(QueryBuilders.nestedQuery("attrs", boolQueryBuilder2, ScoreMode.None));
//                //最外层的bool
//                boolQueryBuilder.filter(boolQueryBuilder1);
//            }
//        }
        if (!StringUtils.isEmpty(props)) {
            Arrays.stream(props)
                    .map(prop -> prop.split(":"))
                    .filter(split -> split.length == 3)
                    .forEach(split -> {
                        //因为这个bool中还嵌入了2个boolquery，所以声明2个bool
                        BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
                        BoolQueryBuilder boolQueryBuilder2 = QueryBuilders.boolQuery();
                        //最里面的bool
                        //赋值Id
                        boolQueryBuilder2.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                        //赋值平台属性名
                        boolQueryBuilder2.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                        //bool - nested
                        boolQueryBuilder1.must(QueryBuilders.nestedQuery("attrs", boolQueryBuilder2, ScoreMode.None));
                        //最外层的bool
                        boolQueryBuilder.filter(boolQueryBuilder1);
                    });
        }
        //query - bool
        searchSourceBuilder.query(boolQueryBuilder);


        //排序
        String order = searchParam.getOrder();
        //排序方式
        if (StringUtils.isEmpty(order)) {
            // 2:desc  2:asc
            String[] split = order.split(":");
            if (split.length == 2) {
                String field = "";
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            } else {
                searchSourceBuilder.sort("hotScore", SortOrder.DESC);
            }
        }

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.postTags("</span>");
        highlightBuilder.preTags("<span style=color:red>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //分页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        //聚合
        //  设置品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                //子聚合
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));

        //  设置平台属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));


        // 结果集过滤
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price"}, null);

        SearchRequest searchRequest = new SearchRequest("goods");
        //searchRequest.types("_doc");
        searchRequest.source(searchSourceBuilder);
        //控制台打印dsl语句
        System.out.println("dsl:" + searchSourceBuilder.toString());
        return searchRequest;
    }

    /**
     * 设置返回结果集
     *
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResponseVo(SearchResponse searchResponse) {
        SearchHits hits = searchResponse.getHits();
        // 声明对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        // 获取总记录数
        searchResponseVo.setTotal(hits.getTotalHits().value);

        // 获取品牌的集合
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        // ParsedLongTerms ?
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo trademark = new SearchResponseTmVo();
            // 获取品牌Id
            trademark.setTmId((Long.parseLong(((Terms.Bucket) bucket).getKeyAsString())));
            //            trademark.setTmId(Long.p`arseLong(bucket.getKeyAsString()));
            // 获取品牌名称
            Map<String, Aggregation> tmIdSubMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdSubMap.get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();

            trademark.setTmName(tmName);

            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdSubMap.get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            trademark.setTmLogoUrl(tmLogoUrl);

            return trademark;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(trademarkList);

        // 赋值商品列表
        SearchHit[] subHits = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        if (subHits != null && subHits.length > 0) {
            // 循环遍历
            Arrays.stream(subHits).forEach(subHit -> {
                // 将subHit 转换为对象
                Goods goods = JSONObject.parseObject(subHit.getSourceAsString(), Goods.class);
                //判断是否有高亮显示
                if (subHit.getHighlightFields().get("title") != null) {
                    // 获取高亮
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    goods.setTitle(title.toString());
                }
                goodsList.add(goods);
            });
        }
        searchResponseVo.setGoodsList(goodsList);

        // 获取平台属性数据
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<SearchResponseAttrVo> searchResponseAttrVOS = buckets.stream().map(bucket -> {
                // 声明平台属性对象
                SearchResponseAttrVo responseAttrVO = new SearchResponseAttrVo();
                // 设置平台属性值Id
                responseAttrVO.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
                List<? extends Terms.Bucket> nameBuckets = attrNameAgg.getBuckets();
                responseAttrVO.setAttrName(nameBuckets.get(0).getKeyAsString());
                // 设置规格参数列表
                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();
                List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                responseAttrVO.setAttrValueList(values);
                return responseAttrVO;
            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(searchResponseAttrVOS);
        }


        return searchResponseVo;
    }

}
