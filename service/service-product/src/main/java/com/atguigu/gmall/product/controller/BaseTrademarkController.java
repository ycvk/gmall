package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("保存品牌数据")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    @ApiOperation("删除品牌数据")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    @ApiOperation("根据品牌Id获取品牌对象")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id) {
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    @ApiOperation("通过ID修改品牌")
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }
}

