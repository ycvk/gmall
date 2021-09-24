package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/22/8:59
 */
public interface SearchService {
    /**
     * 上架商品列表
     *
     * @param skuId
     */
    void upperGoods(Long skuId);

    /**
     * 下架商品列表
     *
     * @param skuId
     */
    void lowerGoods(Long skuId);

    /**
     * 更新热点
     *
     * @param skuId
     */
    void incrHotScore(Long skuId);

    /**
     * 检索方法
     *
     * @param searchParam
     * @return
     */
    SearchResponseVo search(SearchParam searchParam);

}
