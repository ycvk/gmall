package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/28/21:01
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        //单个购物项
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo != null) {
            //如果存在，更新数据
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //如果新添加，则自动选中
            cartInfo.setIsChecked(1);
            //设置最新的实时价格
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            //更新时间
            cartInfo.setUpdateTime(new Date());
        } else {
            //不存在，插入数据
            CartInfo info = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            info.setCartPrice(skuInfo.getPrice());
            info.setSkuPrice(skuInfo.getPrice());
            info.setSkuNum(skuNum.intValue());
            info.setUserId(userId);
            info.setSkuId(skuId);
            info.setSkuName(skuInfo.getSkuName());
            info.setImgUrl(skuInfo.getSkuDefaultImg());
            info.setCreateTime(new Date());
            info.setUpdateTime(new Date());
            cartInfo = info;
        }
        //放入缓存
        redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);

        //设置购物车的过期时间
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        //购物车列表
        List<CartInfo> cartInfoList = null;
        List<CartInfo> loginCartInfoList = null;
        List<CartInfo> noLoginCartInfoList = null;

        if (!StringUtils.isEmpty(userTempId)) {
            //userTempId不为空，未登录状态
            String cartTempKey = RedisConst.USER_KEY_PREFIX + userTempId + RedisConst.USER_CART_KEY_SUFFIX;
            noLoginCartInfoList = redisTemplate.opsForHash().values(cartTempKey);
        }

        if (!StringUtils.isEmpty(userId)) {
            //userId不为空，登录状态
            //获取登录的key
            String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
            loginCartInfoList = redisTemplate.opsForHash().values(cartKey);
        }

        if (!CollectionUtils.isEmpty(loginCartInfoList)) {
            //登陆状态
            if (CollectionUtils.isEmpty(noLoginCartInfoList)) {
                //tempId没有值，未登录状态购物车为空
                cartInfoList = loginCartInfoList;
            } else {
                //未登录状态下购物车有值，且登录状态下也有值，合并
                String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
                BoundHashOperations<String, String, CartInfo> boundHashOperations = this.redisTemplate.boundHashOps(cartKey);
                //  判断购物车中的field
                if (!CollectionUtils.isEmpty(noLoginCartInfoList)) {
                    //  循环遍历未登录购物车集合
                    noLoginCartInfoList.forEach(cartInfo -> {
                        //  在未登录购物车中的skuId 与登录的购物车skuId 相对  skuId = 17 18
                        if (boundHashOperations.hasKey(cartInfo.getSkuId().toString())) {
                            //  合并业务逻辑 : skuNum + skuNum 更新时间
                            CartInfo loginCartInfo = boundHashOperations.get(cartInfo.getSkuId().toString());
                            loginCartInfo.setUpdateTime(new Date());
                            //  最新价格
                            loginCartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));

                            //  选中状态合并！
                            if (cartInfo.getIsChecked().intValue() == 1) {
                                loginCartInfo.setIsChecked(1);
                            }
                            //  修改缓存的数据：    hset key field value
                            boundHashOperations.put(cartInfo.getSkuId().toString(), loginCartInfo);
                        } else {
                            //  直接添加到缓存！    skuId = 19
                            cartInfo.setUserId(userId);
                            cartInfo.setCreateTime(new Date());
                            cartInfo.setUpdateTime(new Date());
                            boundHashOperations.put(cartInfo.getSkuId().toString(), cartInfo);
                        }
                    });
                    this.redisTemplate.delete(RedisConst.USER_KEY_PREFIX + userTempId + RedisConst.USER_CART_KEY_SUFFIX);
                }
                //  获取到合并之后的数据：
                cartInfoList = this.redisTemplate.boundHashOps(cartKey).values();

            }

        } else {
            //未登录状态
            cartInfoList = noLoginCartInfoList;
        }

        //按照更新时间排序并返回
        assert cartInfoList != null;
        return cartInfoList.stream().sorted((o1, o2) -> DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND)).collect(Collectors.toList());

    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = boundHashOps.get(skuId.toString());
        if (null != cartInfo) {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(skuId.toString(), cartInfo);
            //过期时间
            redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
        }
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);
        //  判断购物车中是否有该商品！
        if (boundHashOps.hasKey(skuId.toString())) {
            boundHashOps.delete(skuId.toString());
            redisTemplate.expire(userId, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
        }
    }
}
