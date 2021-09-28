package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author VERO
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {


    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        UserInfo login = userService.login(userInfo);
        if (login != null) {
            //有用户信息
            //生成一个token
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            Map map = new HashMap();
            map.put("token", token);
            map.put("nickName", login.getNickName());

            //用户信息放入缓存
            String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;

            //防止token被盗用，把服务器ip存入缓存
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", login.getId());
            jsonObject.put("ip", IpUtil.getIpAddress(request));

            redisTemplate.opsForValue()
                    .set(loginKey, jsonObject.toString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

            return Result.ok(map);
        } else {
            return Result.fail().message("登陆失败");
        }

    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request) {
        //从请求头中获取token
        String token = request.getHeader("token");
        String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        redisTemplate.delete(loginKey);

        return Result.ok();
    }

}
