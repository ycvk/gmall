package com.atguigu.gmall.common.cache;

import java.lang.annotation.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/18/8:44
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LockCache {

    String prefix() default "lockCache:";

}
