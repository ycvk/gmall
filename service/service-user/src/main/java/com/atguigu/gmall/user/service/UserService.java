package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/27/20:01
 */
public interface UserService {

    /**
     * 登陆方法
     *
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

}
