package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/10/8/19:14
 */
public interface OrderService {

    /**
     * 保存订单
     *
     * @param orderInfo
     * @return
     */
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * 生产流水号
     *
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     *
     * @param userId      获取缓存中的流水号
     * @param tradeCodeNo 页面传递过来的流水号
     * @return
     */
    boolean checkTradeCode(String userId, String tradeCodeNo);


    /**
     * 删除流水号
     *
     * @param userId
     */
    void deleteTradeNo(String userId);

}
