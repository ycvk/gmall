package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Map<String, Object> getItem(Long skuId) {
        //根据skuId获取sku信息
        SkuInfo skuInfo = productFeinClient.getSkuInfo(skuId);
        //根据skuid获取价格信息
        BigDecimal skuPrice = productFeinClient.getSkuPrice(skuId);
        //通过三级分类id查询分类信息
        BaseCategoryView categoryView = productFeinClient.getCategoryView(skuInfo.getCategory3Id());
        //通过skuId 集合来查询数据
        List<BaseAttrInfo> attrList = productFeinClient.getAttrList(skuId);
        //根据spuId 查询map 集合属性
        Map skuValueIdsMap = productFeinClient.getSkuValueIdsMap(skuInfo.getSpuId());
        String strJson = JSON.toJSONString(skuValueIdsMap);
        //根据spuId 获取海报数据
        List<SpuPoster> spuPosterBySpuId = productFeinClient.findSpuPosterBySpuId(skuInfo.getSpuId());
        //根据spuId，skuId 查询销售属性集合
        List<SpuSaleAttr> spuSaleAttrListById = productFeinClient.getSpuSaleAttrListById(skuId, skuInfo.getSpuId());
        HashMap<String, Object> hashMap = new HashMap<>();


        hashMap.put("skuInfo", skuInfo);
        hashMap.put("categoryView", categoryView);
        hashMap.put("spuSaleAttrList", spuSaleAttrListById);
        hashMap.put("valuesSkuJson", strJson);
        hashMap.put("price", skuPrice);
        hashMap.put("spuPosterList", spuPosterBySpuId);

        List<Map<String, String>> skuAttrList = attrList.stream().map((baseAttrInfo) -> {
            Map<String, String> attrMap = new HashMap<>();
            attrMap.put("attrName", baseAttrInfo.getAttrName());
            attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
            return attrMap;
        }).collect(Collectors.toList());

        hashMap.put("skuAttrList", skuAttrList);

        return hashMap;
    }
}
