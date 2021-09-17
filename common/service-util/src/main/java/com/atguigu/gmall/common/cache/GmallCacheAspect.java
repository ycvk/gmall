package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/17/20:45
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    //定义一个环绕通知
    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
        //  声明一个对象
        Object obj = new Object();

        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        //获取方法上的注解
        GmallCache annotation = methodSignature.getMethod().getAnnotation(GmallCache.class);
        //获取注解上的前缀
        String prefix = annotation.prefix();
        //组成缓存key  前缀+方法传递的参数
        String key = prefix + Arrays.asList(point.getArgs()).toString();
        //通过key获取缓存的数据
        try {
            obj = getRedisData(key, methodSignature);
            if (obj == null) {
                //获取分布式锁
                RLock lock = redissonClient.getLock(key + ":lock");
                boolean tryLock = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //创建锁成功
                if (tryLock) {
                    try {
                        //因为obj为空，没有缓存，所以从数据库获取数据
                        obj = point.proceed(point.getArgs());
                        //防止缓存穿透
                        if (obj == null) {
                            Object object = new Object();
                            //因为执行proceed后，无法确定对象类型，所以将缓存数据变为json字符串存入
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return object;
                        } else {
                            //数据不为空，存入redis缓存
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(obj), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            return obj;
                        }
                    } finally {
                        //解锁
                        lock.unlock();
                    }
                } else {
                    try {
                        Thread.sleep(100);
                        return cacheAroundAdvice(point);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //不为空，返回缓存
                return obj;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //redis出现错误，那就直接通过数据库获取并返回
        return point.proceed(point.getArgs());
    }

    /**
     * 从缓存中获取数据
     *
     * @param key
     * @param methodSignature
     * @return
     */
    private Object getRedisData(String key, MethodSignature methodSignature) {
        //从缓存中获取被转换为string的数据
        String str = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(str)) {
            //如果数据不为空，那就转换为对象原来的类型
            return JSON.parseObject(str, methodSignature.getReturnType());
        }
        return null;
    }

}
