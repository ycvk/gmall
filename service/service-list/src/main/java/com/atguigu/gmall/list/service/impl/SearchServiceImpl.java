package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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
}
