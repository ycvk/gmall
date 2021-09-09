package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;


    @ApiOperation("品牌图片分页列表")
    @GetMapping("{page}/{limit}")
    public Result getTrademarkPage(@PathVariable Long page, @PathVariable Long limit) {
        Page<BaseTrademark> page1 = new Page<>(page, limit);
        Page page2 = baseTrademarkService.getTrademarkPage(page1);
        return Result.ok(page2);
    }
}

