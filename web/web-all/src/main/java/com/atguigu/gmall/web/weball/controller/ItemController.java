package com.atguigu.gmall.web.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/15/20:03
 */
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) {
        //获取数据
        Result<Map> item = itemFeignClient.getItem(skuId);
        //存储数据
        model.addAllAttributes(item.getData());
        return "item/index";
    }

}
