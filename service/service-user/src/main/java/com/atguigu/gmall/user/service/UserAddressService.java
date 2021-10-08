package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/10/7/21:48
 */
public interface UserAddressService {


    /**
     * 获取收货地址列表
     *
     * @param userId
     * @return
     */
    List<UserAddress> findUserAddressListByUserId(String userId);
}
