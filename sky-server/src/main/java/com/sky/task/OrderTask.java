package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void processTimeoutOrder() {
        LocalDateTime orderTime = LocalDateTime.now().plusMinutes(-15); // 下单时间小于15分钟
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, orderTime);
        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }

        log.info("处理超时未支付订单：{}", ordersList.size());
        for (Orders orders : ordersList) {
            Orders update = Orders.builder()
                    .id(orders.getId())
                    .status(Orders.CANCELLED)
                    .cancelReason("订单超时，自动取消")
                    .cancelTime(LocalDateTime.now())
                    .build();
            orderMapper.update(update);
        }
    }


    /**
     * 处理处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")  // 每天凌晨1点执行
    public void processDeliveryOrder() {
        LocalDateTime orderTime = LocalDateTime.now().plusHours(-1); // 下单时间小于1小时
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, orderTime);
        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }

        log.info("处理配送中自动完成订单：{}", ordersList.size());
        for (Orders orders : ordersList) {
            Orders update = Orders.builder()
                    .id(orders.getId())
                    .status(Orders.COMPLETED)
                    .build();
            orderMapper.update(update);
        }
    }
}
