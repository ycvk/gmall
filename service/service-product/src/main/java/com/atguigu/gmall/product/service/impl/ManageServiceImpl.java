package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/7/20:02
 */
@Service
public class ManageServiceImpl implements ManageService {


    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    //??????????????????skuinfo
    private SkuInfo getSkuInfoFromDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo != null) {
            skuInfo.setSkuImageList(skuImageMapper
                    .selectList(new QueryWrapper<SkuImage>()
                            .eq("sku_id", skuId)));
        }

        return skuInfo;
    }

    /**
     * ??????????????????????????????
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * ????????????ID????????????????????????
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>()
                .eq("category1_id", category1Id));
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>()
                .eq("category2_id", category2Id));
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectAttrInfoList(category1Id, category2Id, category3Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) //?????????????????????
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //??????id???????????????????????????????????????
        if (baseAttrInfo.getId() == null) {
            //????????????
            baseAttrInfoMapper.insert(baseAttrInfo);
        } else {
            //??????
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //??????-->?????????value???????????????
            baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>()
                    .eq("attr_id", baseAttrInfo.getId()));
        }

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //??????baseAttrInfo??????baseAttrValue???????????????
        attrValueList.forEach(baseAttrValue -> {
            //attId??????????????????null
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(baseAttrValue);
        });
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        return baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>()
                .eq("attr_id", attrId));
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        //????????????????????????value??????
        if (baseAttrInfo != null) {
            baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        }
        return baseAttrInfo;
    }

    @Override
    public Page getSpuInfoPage(Page spuPage, SpuInfo spuInfo) {
        return spuInfoMapper.selectPage(spuPage, new QueryWrapper<SpuInfo>()
                .eq("category3_id", spuInfo.getCategory3Id())
                .orderByDesc("id"));
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insert(spuInfo);
        //???????????????
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)) {
            spuImageList.forEach(spuImage -> {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            });
        }

        //????????????
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                //???????????????????????????
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        //??????
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });

                }
            });
        }

        //??????spuposter
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            spuPosterList.forEach(spuPoster -> {
                spuPoster.setSpuId(spuInfo.getId());
                spuPosterMapper.insert(spuPoster);
            });
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id", spuId));
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        /*
        skuInfo ??????????????? --- spuInfo???
        skuImage ????????????????????? --- spuImage!
        skuSaleAttrValue sku??????????????????{sku??????????????????????????????} --- skuInfo ???spuSaleAttrValue
        skuAttrValue sku?????????????????????????????? --- skuInfo ???baseAttrValue
        */
        skuInfoMapper.insert(skuInfo);
        //??????image??????
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }
        //sku  attrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());

                skuAttrValueMapper.insert(skuAttrValue);
            });
        }

        //sku sale attrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }
    }

    @Override
    public Page getList(Page<SkuInfo> skuInfoPage) {
        return skuInfoMapper.selectPage(skuInfoPage, new QueryWrapper<SkuInfo>()
                .orderByDesc("id"));

    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    @GmallCache(prefix = "sku:")
    public SkuInfo getSkuInfo(Long skuId) {
//        return getInfoViaRedisson(skuId);
        return getSkuInfoFromDB(skuId);
    }

    //???redisson???????????????????????????
    public SkuInfo getInfoViaRedisson(Long skuId) {
        //???????????????????????????????????????????????????????????????
        //???????????????key   sku:skuId:info
        String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        try {
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //?????????????????????
            if (skuInfo == null) {
                //??????????????????key  sku:skuId:lock
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                boolean flag = false;
                try {
                    flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (flag) {
                    try {
                        //??????????????????
                        skuInfo = getSkuInfoFromDB(skuId);
                        //??????????????????
                        if (skuInfo == null) {
                            //??????????????????????????????
                            SkuInfo nullSkuInfo = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey, nullSkuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return nullSkuInfo;
                        } else {
                            //????????????????????????????????????????????????
                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            //????????????
                            return skuInfo;
                        }
                    } finally {
                        //??????
                        lock.unlock();
                    }
                }
            } else {
                //??????????????????
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //redis????????????
        return getInfoViaRedisLua(skuId);
    }

    //??????redis+lua??????????????????
    public SkuInfo getInfoViaRedisLua(Long skuId) {
        //???????????????????????????????????????????????????????????????
        try {
            //???????????????key   sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //?????????????????????
            if (skuInfo == null) {
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                //??????????????????key  sku:skuId:lock
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                //??????
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                if (flag) {
                    //??????????????????
                    skuInfo = getSkuInfoFromDB(skuId);
                    //??????????????????
                    if (skuInfo == null) {
                        //??????????????????????????????
                        SkuInfo nullSkuInfo = new SkuInfo();
                        redisTemplate.opsForValue().set(skuKey, nullSkuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        return nullSkuInfo;
                    } else {
                        //????????????????????????????????????????????????
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        // ???????????????lua ????????????
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        // ??????lua???????????????????????????
                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                        // ??????lua?????????????????????Long
                        redisScript.setResultType(Long.class);
                        redisScript.setScriptText(script);
                        // ??????key ???????????? value
                        redisTemplate.execute(redisScript, Arrays.asList(lockKey), uuid);
                        //????????????
                        return skuInfo;
                    }
                } else {
                    //???????????????
                    try {
                        Thread.sleep(1000);
                        //??????
                        getSkuInfo(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //?????????????????????????????????????????????
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //redis???????????????????????????????????????????????????
        return getSkuInfoFromDB(skuId);
    }


    @Override
    public BigDecimal getPrice(Long skuId) {
        RLock lock = redissonClient.getLock("priceLock");
        lock.lock();
        SkuInfo skuInfo = null;
        BigDecimal price = new BigDecimal(0);
        try {
            skuInfo = skuInfoMapper.selectOne(new QueryWrapper<SkuInfo>()
                    .eq("id", skuId)
                    .select("price"));
            if (skuInfo != null) {
                price = skuInfo.getPrice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return price;
    }

    @Override
    @GmallCache(prefix = "CategoryViewByCategory3Id")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "SpuSaleAttrListById")
    public List<SpuSaleAttr> getSpuSaleAttrListById(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListById(skuId, spuId);
    }

    @Override
    @GmallCache(prefix = "SkuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        HashMap<Object, Object> hashMap = new HashMap<>();

        List<Map> mapList = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        if (!CollectionUtils.isEmpty(mapList)) {
            mapList.forEach(map -> {
                hashMap.put(map.get("value_ids"), map.get("sku_id"));
            });
        }
        return hashMap;
    }

    @Override
    @GmallCache(prefix = "SpuPoster:")
    public List<SpuPoster> getSpuPosterById(Long spuId) {
        return spuPosterMapper.selectList(new QueryWrapper<SpuPoster>().eq("spu_id", spuId));
    }

    @Override
    @GmallCache(prefix = "BaseAttrInfoList:")
    public List<BaseAttrInfo> getBaseAttrInfoList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoList(skuId);
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {

        List<JSONObject> list = new ArrayList<>();
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //??????category1Id????????????
        Map<Long, List<BaseCategoryView>> baseCategory1Map =
                baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        int index = 1;
        //??????
        Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator = baseCategory1Map.entrySet().iterator();
        while (iterator.hasNext()) {
            //????????????????????????
            JSONObject category1 = new JSONObject();
            Map.Entry<Long, List<BaseCategoryView>> next = iterator.next();
            Long category1Id = next.getKey();
            List<BaseCategoryView> baseCategoryViewList1 = next.getValue();
            //????????????Id
            category1.put("categoryId", category1Id);
            //??????????????????
            category1.put("categoryName", baseCategoryViewList1.get(0).getCategory1Name());
            //????????????????????????
            category1.put("index", index);
            index++;
            //????????????????????????????????????
            Map<Long, List<BaseCategoryView>> baseCategory2Map = baseCategoryViewList1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator1 = baseCategory2Map.entrySet().iterator();
            //??????????????????2???JSONObject?????????list
            ArrayList<JSONObject> category2List = new ArrayList<>();
            while (iterator1.hasNext()) {
                Map.Entry<Long, List<BaseCategoryView>> next1 = iterator1.next();
                Long category2Id = next1.getKey();
                List<BaseCategoryView> baseCategoryViewList2 = next1.getValue();
                JSONObject category2 = new JSONObject();
                //2?????????Id
                category2.put("categoryId", category2Id);
                //2???????????????
                category2.put("categoryName", baseCategoryViewList2.get(0).getCategory2Name());

                //????????????????????????
                //????????????list?????????3???????????????
                ArrayList<JSONObject> category3List = new ArrayList<>();
                baseCategoryViewList2.stream().forEach(baseCategoryView -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId", baseCategoryView.getCategory3Id());
                    category3.put("categoryName", baseCategoryView.getCategory3Name());
                    category3List.add(category3);
                });
                //?????????????????????????????????list???
                category2.put("categoryChild", category3List);
                category2List.add(category2);
            }
            //???2??????????????????1??????list???
            category1.put("categoryChild", category2List);

            //???1??????????????????list???
            list.add(category1);
        }

        return list;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }
}
