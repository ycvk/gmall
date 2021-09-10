package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/10/18:59
 */
@Service
public class BaseCategoryTrademarkServiceImpl implements BaseCategoryTrademarkService {
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        //通过category3Id在base_category_trademark表中获取数据
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(new QueryWrapper<BaseCategoryTrademark>()
                .eq("category3_id", category3Id));
        //如果list不为空，继续
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            ArrayList<Long> arrayList =
                    baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId)
                            .collect(Collectors.toCollection(ArrayList::new));
            return baseTrademarkMapper.selectBatchIds(arrayList);
        }
        return null;
    }
}
