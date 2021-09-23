package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @date 2021/9/14/18:51
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeinClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getItem(Long skuId) {
        //声明集合对象
        HashMap<String, Object> hashMap = new HashMap<>();
        //使用异步编排方式
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //根据skuId获取sku信息
            SkuInfo skuInfo = productFeinClient.getSkuInfo(skuId);
            hashMap.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        //根据skuid获取价格信息
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeinClient.getSkuPrice(skuId);
            hashMap.put("price", skuPrice);
        }, threadPoolExecutor);

        //通过三级分类id查询分类信息
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeinClient.getCategoryView(skuInfo.getCategory3Id());
            hashMap.put("categoryView", categoryView);
        }, threadPoolExecutor);

        //通过skuId 集合来查询数据
        CompletableFuture<Void> attrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeinClient.getAttrList(skuId);
            List<Map<String, String>> skuAttrList = attrList.stream().map((baseAttrInfo) -> {
                Map<String, String> attrMap = new HashMap<>();
                attrMap.put("attrName", baseAttrInfo.getAttrName());
                attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return attrMap;
            }).collect(Collectors.toList());
            hashMap.put("skuAttrList", skuAttrList);
        }, threadPoolExecutor);

        //根据spuId 查询map 集合属性
        CompletableFuture<Void> valueJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeinClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String strJson = JSON.toJSONString(skuValueIdsMap);
            hashMap.put("valuesSkuJson", strJson);
        }, threadPoolExecutor);

        //根据spuId 获取海报数据
        CompletableFuture<Void> spuPosterListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuPoster> spuPosterBySpuId = productFeinClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            hashMap.put("spuPosterList", spuPosterBySpuId);
        }, threadPoolExecutor);

        //根据spuId，skuId 查询销售属性集合
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListById = productFeinClient.getSpuSaleAttrListById(skuId, skuInfo.getSpuId());
            hashMap.put("spuSaleAttrList", spuSaleAttrListById);
        }, threadPoolExecutor);

        //热度排名
        CompletableFuture<Void> hotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);

        //返回数据
        CompletableFuture.allOf(skuInfoCompletableFuture,
                priceCompletableFuture,
                categoryViewCompletableFuture,
                attrCompletableFuture,
                valueJsonCompletableFuture,
                spuPosterListCompletableFuture,
                spuSaleAttrListCompletableFuture,
                hotScoreCompletableFuture).join();
        return hashMap;
    }
}
