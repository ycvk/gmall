package com.atguigu.gmall.web.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/24/20:42
 */
@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("list.html")
    public String listPage(SearchParam searchParam, Model model) {
        Result<Map> result = listFeignClient.searchData(searchParam);
        //存入获取到的数据
        model.addAllAttributes(result.getData());
        model.addAttribute("searchParam", searchParam);

        String urlParam = makeUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);

        //品牌面包屑
        String trademarkParam = makeTrademarkParam(searchParam.getTrademark());
        model.addAttribute("trademarkParam", trademarkParam);

        //props集合
        ArrayList<Map> propsParamList = makePropsParamList(searchParam.getProps());
        model.addAttribute("propsParamList", propsParamList);


        //设置排序规则
        Map<String, Object> orderMap = makeOrderMap(searchParam.getOrder());
        model.addAttribute("orderMap", orderMap);
        return "list/index";
    }

    private Map<String, Object> makeOrderMap(String order) {
        Map<String, Object> orderMap = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {
            //获取排序规则
            String[] split = order.split(":");
            if (split.length == 2) {
                orderMap.put("type", split[0]);
                orderMap.put("sort", split[1]);
            }
        } else {
            orderMap.put("type", "1");
            orderMap.put("sort", "asc");
        }

        return orderMap;
    }

    private ArrayList<Map> makePropsParamList(String[] props) {
        ArrayList<Map> maps = new ArrayList<>();

        if (props != null && props.length > 0) {
            Arrays.stream(props)
                    .map(prop -> prop.split(":"))
                    .filter(split -> split.length == 3)
                    .forEach(split -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("attrId", split[0]);
                        hashMap.put("attrValue", split[1]);
                        hashMap.put("attrName", split[2]);
                        maps.add(hashMap);
                    });
        }
        return maps;
    }

    //获取品牌面包屑
    private String makeTrademarkParam(String trademark) {
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split.length == 2) {
                return "品牌：" + split[1];
            }
        }

        return "";
    }

    /**
     * 制作url地址
     *
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        //线程安全的stringBuffer
        StringBuffer str = new StringBuffer();
        //需要知道是按照哪种方式进行检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            //按照关键字检索
            str.append("keyword=").append(searchParam.getKeyword());
        }

        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            //按照3级分类Id进行检索
            str.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            //按照2级分类Id进行检索
            str.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            //按照1级分类Id进行检索
            str.append("category1Id=").append(searchParam.getCategory1Id());
        }

        //按照平台属性值过滤
        String[] props = searchParam.getProps();
        if (!StringUtils.isEmpty(props)) {
            Arrays.stream(props)
                    .filter(prop -> str.length() > 0)
                    .forEach(prop -> str.append("&props=").append(prop));
        }

        //通过品牌过滤
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            if (str.length() > 0) {
                str.append("&trademark=").append(trademark);
            }


        }
        return "list.html?" + str.toString();
    }
}
