package com.atguigu.gmall.cart.client.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/10/7/16:21
 */
@Component
public class CartDegradeFeignClient implements CartFeignClient {

    @Override
    public Result addToCart(Long skuId, Integer skuNum) {
        return null;
    }
}
