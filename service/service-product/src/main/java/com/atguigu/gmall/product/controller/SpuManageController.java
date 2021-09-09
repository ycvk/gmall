package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author VERO
 */
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;

    @ApiOperation("spu分页列表")
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo) {
        //创建分页
        Page spuPage = new Page(page, limit);

        Page spuInfoPage = manageService.getSpuInfoPage(spuPage, spuInfo);
        return Result.ok(spuInfoPage);
    }
}

