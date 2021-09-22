package com.atguigu.gmall.product.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
 * @date 2021/9/14/19:42
 */
@RestController
@RequestMapping("api/product")
public class ProductApiController {
    @Autowired
    private ManageService manageService;

    /**
     * 根据skuId获取sku信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        return manageService.getSkuInfo(skuId);
    }

    /**
     * 根据skuid获取价格信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId) {
        return manageService.getPrice(skuId);
    }

    /**
     * 通过三级分类id查询分类信息
     *
     * @param category3Id
     * @return
     */
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {
        return manageService.getCategoryViewByCategory3Id(category3Id);
    }

    /**
     * 根据spuId，skuId 查询销售属性集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListById(@PathVariable Long skuId,
                                                    @PathVariable Long spuId) {
        return manageService.getSpuSaleAttrListById(skuId, spuId);
    }

    /**
     * 根据spuId 查询map 集合属性
     *
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId) {
        return manageService.getSkuValueIdsMap(spuId);
    }

    /**
     * 根据spuId 获取海报数据
     *
     * @param spuId
     * @return
     */
    @GetMapping("inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId) {
        return manageService.getSpuPosterById(spuId);
    }

    /**
     * 通过skuId 集合来查询数据
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId) {
        return manageService.getBaseAttrInfoList(skuId);
    }


    @GetMapping("getBaseCategoryList")
    public List<JSONObject> getBaseCategoryList() {
        return manageService.getBaseCategoryList();
    }

    /**
     * 通过品牌Id 集合来查询数据
     *
     * @param tmId
     * @return
     */
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable("tmId") Long tmId) {
        return manageService.getTrademarkByTmId(tmId);
    }

}
