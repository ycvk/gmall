package com.atguigu.gmall.web.weball.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/21/22:05
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @GetMapping({"/", "index.html"})
    public String index(Model model) {

        List<JSONObject> baseCategoryList = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", baseCategoryList);

        return "index/index";
    }
}
