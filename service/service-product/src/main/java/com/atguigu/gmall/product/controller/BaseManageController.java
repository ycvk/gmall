package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/7/19:52
 */
@Api
@RestController
@RequestMapping("/admin/product")
public class BaseManageController {

    //调用服务层接口
    @Autowired
    private ManageService manageService;


    /**
     * @return
     */
    @ApiOperation("获取所有一级分类信息")
    @GetMapping("getCategory1")
    public Result getCategory1() {
        List<BaseCategory1> baseCategory1List = manageService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    @ApiOperation("根据一级Id获取所有二级分类信息")
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id) {
        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    @ApiOperation("根据二级Id获取三级分类信息")
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id) {
        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    @ApiOperation("根据分类Id 获取平台属性集合")
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getAttrInfoList(@PathVariable Long category1Id,
                                  @PathVariable Long category2Id,
                                  @PathVariable Long category3Id) {
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(baseAttrInfoList);
    }

    @ApiOperation("新增或修改平台属性")
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }


    @ApiOperation("根据平台属性Id 获取到平台属性值集合")
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId) {
        List<BaseAttrValue> baseAttrValueList = manageService.getAttrValueList(attrId);
        return Result.ok(baseAttrValueList);
    }

}
