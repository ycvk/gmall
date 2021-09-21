//package com.atguigu.gmall.common.cache;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//
///**
// * <p>Title: </p>
// *
// * <p>Description: </p>
// *
// * @author VERO
// * @version 1.0
// * @date 2021/9/18/8:48
// */
//@Component
//@Aspect
//public class LockCacheAspect {
//
//    @Autowired
//    private RedissonClient redissonClient;
//
//    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
//
//        Object obj = new Object();
//
//        MethodSignature methodSignature = (MethodSignature) point.getSignature();
//        //获取方法上的注解
//        GmallCache annotation = methodSignature.getMethod().getAnnotation(GmallCache.class);
//        //获取注解上的前缀
//        String prefix = annotation.prefix();
//        //组成缓存key  前缀+方法传递的参数
//        String key = prefix + Arrays.asList(point.getArgs()).toString();
//
//        RLock lock = redissonClient.getLock(key);
//        lock.lock();
//        SkuInfo skuInfo = null;
//        BigDecimal price = new BigDecimal(0);
//        try {
//            skuInfo = skuInfoMapper.selectOne(new QueryWrapper<SkuInfo>()
//                    .eq("id", skuId)
//                    .select("price"));
//            if (skuInfo != null) {
//                price = skuInfo.getPrice();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            lock.unlock();
//        }
//        return price;
//
//
//        return obj;
//    }
//}
