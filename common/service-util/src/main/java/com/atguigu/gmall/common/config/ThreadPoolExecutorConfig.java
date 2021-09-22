package com.atguigu.gmall.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/21/20:17
 */
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public java.util.concurrent.ThreadPoolExecutor threadPoolExecutor() {
        return new java.util.concurrent.ThreadPoolExecutor(12,
                100,
                3L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
    }

}
