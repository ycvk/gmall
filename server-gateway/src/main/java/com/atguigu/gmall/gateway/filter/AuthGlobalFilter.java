package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/28/9:10
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    //从配置文件获取 访问时会拦截并跳转到登陆的页面网址路径 (trade.html,myOrder.html,list.html)
    @Value("${authUrls.url}")
    private String authUrls;

    // 匹配路径的工具类
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求的对象
        ServerHttpRequest request = exchange.getRequest();
        //获取请求的路径
        String path = request.getURI().getPath();

        //判断/**/inner/**
        if (antPathMatcher.match("/**/inner/**", path)) {
            //路径匹配的话:
            ServerHttpResponse response = exchange.getResponse();
            //停止运行
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //获取用户Id
        String userId = getUserId(request);
        if ("-1".equals(userId)) {
            return out(exchange.getResponse(), ResultCodeEnum.PERMISSION);
        }

        if (antPathMatcher.match("/api/**/auth/**", path)) {
            //路径匹配的话:
            //用户必须登录状态下才能继续，否则不能
            if (StringUtils.isEmpty(userId)) {
                //为空，未登录状态，提示要登录
                ServerHttpResponse response = exchange.getResponse();
                //停止运行
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //用户访问哪些页面需要跳转到登录页面
        String[] split = authUrls.split(",");
        for (String url : split) {
            //用户访问的控制器在authUrls中存在，并且用户未登录
            if (path.contains(url) && StringUtils.isEmpty(userId)) {
                //跳转到登录
                //获取响应对象
                ServerHttpResponse response = exchange.getResponse();
                //设置响应参数
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //设置请求头
                response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                //重定向
                return response.setComplete();
            }
        }

        //将获取的用户Id放入请求头中
        if (!StringUtils.isEmpty(userId)) {
            request.mutate().header("userId", userId).build();

            return chain.filter(exchange.mutate().request(request).build());
        }

        //默认返回
        return chain.filter(exchange);
    }

    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {

        Result<Object> result = Result.build(null, resultCodeEnum);
        String str = JSON.toJSONString(result);
        DataBuffer wrap = response.bufferFactory().wrap(str.getBytes());
        //设置请求头字符集
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * 获取userId
     *
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        HttpCookie httpCookie = request.getCookies().getFirst("token");
        if (httpCookie != null) {
            token = httpCookie.getValue();
        } else {
            List<String> list = request.getHeaders().get("token");
            if (!CollectionUtils.isEmpty(list)) {
                token = list.get(0);
            }
        }
        //获取到token之后，组成缓存的key来获取数据
        if (!StringUtils.isEmpty(token)) {
            String userKey = "user:login:" + token;
            String string = (String) redisTemplate.opsForValue().get(userKey);
            JSONObject jsonObject = JSONObject.parseObject(string);
            String ip = (String) jsonObject.get("ip");
            //判断ip是否一致
            if (ip.equals(IpUtil.getGatwayIpAddress(request))) {
                return jsonObject.getString("userId");
            } else {
                return "-1";
            }
        }
        return null;
    }

}
