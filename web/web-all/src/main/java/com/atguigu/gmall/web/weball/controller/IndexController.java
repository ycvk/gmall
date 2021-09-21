package com.atguigu.gmall.web.weball.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;
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

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping({"/", "index.html"})
    public String index(Model model) {

        List<JSONObject> baseCategoryList = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", baseCategoryList);

        return "index/index";
    }

    /**
     * nginx做静态代理方式生成静态页面
     */
    @GetMapping("createIndex")
    public Result createIndex() {
        //  获取后台存储的数据
        List<JSONObject> baseCategoryList = productFeignClient.getBaseCategoryList();
        //  设置模板显示的内容
        Context context = new Context();
        context.setVariable("list", baseCategoryList);

        //  定义文件输入位置
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("D:\\index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  调用process();方法创建模板
        templateEngine.process("index/index.html", context, fileWriter);
        return Result.ok();
    }
}
