package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/21/23:39
 */
@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @GetMapping("inner/createIndex")
    public Result createIndex() {
        //自动创建mapping
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }

}
