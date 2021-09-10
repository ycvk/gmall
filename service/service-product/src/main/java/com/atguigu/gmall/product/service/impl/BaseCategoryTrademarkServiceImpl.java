package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {
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

    @Override
    public void remove(Long category3Id, Long trademarkId) {
        baseCategoryTrademarkMapper.delete(new QueryWrapper<BaseCategoryTrademark>()
                .eq("category3_id", category3Id)
                .eq("trademark_id", trademarkId));
    }

    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {

        //通过category3Id在base_category_trademark表中获取数据
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(new QueryWrapper<BaseCategoryTrademark>()
                .eq("category3_id", category3Id));
        //如果list不为空，继续
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            ArrayList<Long> arrayList =
                    baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId)
                            .collect(Collectors.toCollection(ArrayList::new));

            return baseTrademarkMapper.selectList(null).stream()
                    .filter(baseTrademark -> !arrayList.contains(baseTrademark.getId()))
                    .collect(Collectors.toList());
        }
        //当分类ID下没有品牌的时候，显示所有品牌数据
        return baseTrademarkMapper.selectList(null);
    }

    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        //如果list不为空，继续
        if (!CollectionUtils.isEmpty(trademarkIdList)) {
            //用forEach方式
//            ArrayList<BaseCategoryTrademark> baseCategoryTrademarks = new ArrayList<BaseCategoryTrademark>;
//            //遍历并把Category3Id与TrademarkId关联
//            trademarkIdList.forEach(aLong -> {
//                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
//                baseCategoryTrademark.setTrademarkId(aLong);
//                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
//                baseCategoryTrademarks.add(baseCategoryTrademark);
//            });
//            this.saveBatch(baseCategoryTrademarks);
            //用stream流方式
            List<BaseCategoryTrademark> collect = trademarkIdList.stream().map((aLong) -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setTrademarkId(aLong);
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
                return baseCategoryTrademark;
            }).collect(Collectors.toList());

            this.saveBatch(collect);
        }
    }
}
