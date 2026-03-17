package com.example.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.canteen.common.BusinessException;
import com.example.canteen.dto.OrderRequest;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Order;
import com.example.canteen.entity.User;
import com.example.canteen.mapper.OrderMapper;
import com.example.canteen.service.DishService;
import com.example.canteen.service.OrderService;
import com.example.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private DishService dishService;
    @Autowired
    private UserService userService;

    /**
     * 实现下单逻辑：保持你之前的乐观锁扣减
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及库存和订单，必须加事务
    public boolean placeOrder(OrderRequest request) {
        // 1. 获取用户信息和菜品信息
        User user = userService.getById(request.getUserId());
        Dish dish = dishService.getById(request.getDishId());

        // 校验：菜品是否存在以及库存是否充足
        if (dish == null || dish.getStock() < request.getQuantity()) {
            throw new BusinessException("库存不足或菜品不存在");
        }

        // 2. 核心：扣减库存（使用乐观锁思想 ge stock）
        boolean ok = dishService.update()
                .setSql("stock = stock - " + request.getQuantity())
                .eq("id", dish.getId())
                .ge("stock", request.getQuantity())
                .update();

        if (!ok) return false;

        // 3. 创建唯一的订单对象，整合所有数据
        Order order = new Order();

        // 绑定用户信息
        order.setUserId(request.getUserId());
        order.setAddress(user.getAddress()); // 关键：快照保存下单地址

        // 绑定菜品信息快照（冗余字段防止菜品删改后找不到数据）
        order.setDishId(request.getDishId());
        order.setDishName(dish.getName());
        order.setQuantity(request.getQuantity());

        // 计算金额与成本
        BigDecimal amount = dish.getPrice().multiply(new BigDecimal(request.getQuantity()));
        BigDecimal costPrice = (dish.getCost() != null) ? dish.getCost() : dish.getPrice().multiply(new BigDecimal("0.6"));
        BigDecimal totalCost = costPrice.multiply(new BigDecimal(request.getQuantity()));

        order.setTotalAmount(amount);
        order.setTotalCost(totalCost); // 锁定当前成本，用于后续利润统计

        // 设置订单状态与时间
        order.setStatus("1"); // 统一使用字符串 "1" 表示待出餐
        order.setOrderTime(LocalDateTime.now());

        // 4. 保存订单
        return this.save(order);
    }

    /**
     * 计算图表的核心数据：总营收、总单数、总利润
     */
    @Override
    public Map<String, Object> getStatistics(String range) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        String dateFormat; // 用于图表分组的格式

        // 1. 根据 range 确定统计范围和图表精度
        switch (range) {
            case "week":
                startTime = now.minusDays(7);
                dateFormat = "%m-%d"; // 按天展示
                break;
            case "month":
                startTime = now.minusMonths(1);
                dateFormat = "%m-%d"; // 按天展示
                break;
            case "year":
                startTime = now.minusYears(1);
                dateFormat = "%Y-%m"; // 按月展示
                break;
            default: // day
                startTime = now.with(LocalTime.MIN);
                dateFormat = "%H:00";
                break;
        }

        // 2. 计算总营收、成交单数、利润（SQL 在 OrderMapper.xml）
        Map<String, Object> overview = this.baseMapper.getStatisticsOverview(startTime);

        // 3. 获取趋势图数据（SQL 在 OrderMapper.xml）
        List<Map<String, Object>> chart = this.baseMapper.getStatisticsChart(startTime, dateFormat);

        result.put("overview", overview);
        result.put("chart", chart);
        return result;
    }

    @Override
    public Map<String, Object> getTodayOverview() {
        String today = LocalDate.now().toString();
        Map<String, Object> stats = this.baseMapper.getTodayStats(today);

        // 计算利润 (简单逻辑：营收 * 0.4，复杂逻辑需减去 dish 的 cost)
        BigDecimal revenue = new BigDecimal(stats.get("revenue").toString());
        BigDecimal profit = revenue.multiply(new BigDecimal("0.4")); // 假设毛利40%

        Map<String, Object> overview = new HashMap<>();
        overview.put("revenue", revenue);
        overview.put("profit", profit.setScale(2, RoundingMode.HALF_UP));
        overview.put("orderCount", stats.get("orderCount"));

        return overview;
    }

    /**
     * 2. 实现 getSevenDaysTrend (近7天趋势)
     * 逻辑：按日期分组统计过去7天的营业额
     */
    @Override
    public List<Map<String, Object>> getSevenDaysTrend() {
        String sevenDaysAgo = LocalDate.now().minusDays(7).toString();
        return this.baseMapper.getSevenDaysTrend(sevenDaysAgo);
    }

    @Override
    public List<Order> getLatestOrders(int limit) {
        return this.baseMapper.selectLatestOrders(limit);
    }
}