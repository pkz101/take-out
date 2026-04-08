package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 历史订单分页查询
     * @param
     * @return
     */
    PageResult pageQuery(Integer page, Integer pageSize, Integer status);

    /**
     * 订单详情
     * @param id
     * @return
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 用户取消订单
     * @param id
     */
    void userCancelById(Long id) throws Exception;

    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);

    /**
     * 条件搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult search(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 统计订单数据
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 订单确认
     * @param orderConfirmDTO
     */
    void confirm(OrdersConfirmDTO orderConfirmDTO);

    /**
     * 订单拒单
     * @param ordersRejectionDTO
     */
    void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 订单取消
     * @param
     */
    void Cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 订单派送
     * @param id
     */
    void delivery(Long id);

    /**
     * 订单完成
     * @param id
     */
    void complete(Long id);
}
