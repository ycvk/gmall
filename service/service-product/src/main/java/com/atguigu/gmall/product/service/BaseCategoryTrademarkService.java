package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;

import java.util.List;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/10/18:58
 */
public interface BaseCategoryTrademarkService {
    /**
     * 根据category3Id获取品牌列表
     *
     * @param category3Id
     * @return
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);
}
