package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/14/23:25
 */
@FeignClient(value = "service-product", fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {
    @GetMapping("api/product/inner/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("api/product/inner/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable Long skuId);

    @GetMapping("api/product/inner/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);

    @GetMapping("api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttrListById(@PathVariable Long skuId,
                                             @PathVariable Long spuId);

    @GetMapping("api/product/inner/getSkuValueIdsMap/{spuId}")
    Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    @GetMapping("api/product/inner/findSpuPosterBySpuId/{spuId}")
    List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId);

    @GetMapping("api/product/inner/getAttrList/{skuId}")
    List<BaseAttrInfo> getAttrList(@PathVariable Long skuId);

    /**
     * 获取全部分类信息
     *
     * @return
     */
    @GetMapping("/api/product/getBaseCategoryList")
    List<JSONObject> getBaseCategoryList();


    /**
     * 通过品牌Id 集合来查询数据
     *
     * @param tmId
     * @return
     */
    @GetMapping("/api/product/inner/getTrademark/{tmId}")
    BaseTrademark getTrademark(@PathVariable("tmId") Long tmId);

}
