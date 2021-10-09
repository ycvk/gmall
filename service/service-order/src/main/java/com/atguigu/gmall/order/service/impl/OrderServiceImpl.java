package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/10/8/19:16
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo) {
        //插入数据到orderInfo表
        //计算总金额
        orderInfo.sumTotalAmount();
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //订单交易编号
        String outTradeNo = UUID.randomUUID().toString().replaceAll("-", "");
        orderInfo.setOutTradeNo(outTradeNo);
        //订单体
        //将所有商品名称进行拼接
        StringBuilder stringBuilder = new StringBuilder();
        orderInfo.getOrderDetailList().stream()
                .map(OrderDetail::getSkuName)
                .forEach(stringBuilder::append);
        if (stringBuilder.length() > 200) {
            orderInfo.setTradeBody(stringBuilder.substring(0, 200));
        } else {
            orderInfo.setTradeBody(stringBuilder.toString());
        }
        //设置时间
        orderInfo.setOperateTime(new Date());
        //过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        //订单状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        //支付方式。默认设为在线支付
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        orderInfoMapper.insert(orderInfo);

        //插入数据到orderDetail表
        orderInfo.getOrderDetailList().forEach(orderDetail -> {
            //订单Id
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        });
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        // 定义一个流水号
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        //从缓存中获取流水号与页面的比较
        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        return tradeCodeNo.equals(redisTradeNo);
    }

    @Override
    public void deleteTradeNo(String userId) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        // 删除数据
        redisTemplate.delete(tradeNoKey);

    }
}
