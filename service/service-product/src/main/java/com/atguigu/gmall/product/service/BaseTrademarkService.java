package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/9/22:31
 */
public interface BaseTrademarkService {
    /**
     * 获取品牌图标分页列表
     *
     * @param page1
     * @return
     */
    Page getTrademarkPage(Page<BaseTrademark> page1);

}
