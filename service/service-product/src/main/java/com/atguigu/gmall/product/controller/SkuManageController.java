package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/12/21:12
 */
@RestController
@RequestMapping("/admin/product")
public class SkuManageController {

    @Autowired
    private ManageService manageService;

    @ApiOperation("保存SkuInfo")
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @ApiOperation("sku分页列表")
    @GetMapping("list/{page}/{limit}")
    public Result getList(@PathVariable Long page,
                          @PathVariable Long limit) {
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        Page page1 = manageService.getList(skuInfoPage);
        return Result.ok(page1);
    }
}
