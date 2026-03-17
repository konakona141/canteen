package com.example.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.canteen.dto.OrderRequest;
import com.example.canteen.entity.Order;

import java.util.List;
import java.util.Map;


public interface OrderService extends IService<Order> {

    boolean placeOrder(OrderRequest request);

    // 2. 新增经营统计功能 (给大屏用)
    Map<String, Object> getStatistics(String range);

    Map<String, Object> getTodayOverview();
    List<Map<String, Object>> getSevenDaysTrend();

    /** 最新 N 条订单（管理端仪表盘） */
    List<Order> getLatestOrders(int limit);
}