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
     * 实现下单逻辑：乐观锁扣减库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean placeOrder(OrderRequest request) {
        // 1. 获取用户信息和菜品信息
        User user = userService.getById(request.getUserId());
        Dish dish = dishService.getById(request.getDishId());

        // 校验：菜品是否存在以及库存是否充足
        if (dish == null || dish.getStock() < request.getQuantity()) {
            throw new BusinessException("库存不足或菜品不存在");
        }

        // 校验：必须选择配送方式
        if (request.getDeliveryType() == null) {
            throw new BusinessException("请选择配送方式");
        }

        // 校验：预售商品必须填写配送/取货时间段
        if (Integer.valueOf(1).equals(dish.getSaleType())) {
            if (request.getDeliveryTime() == null) {
                throw new BusinessException("预售商品请填写配送或取货时间段");
            }
        }

        // 2. 核心：扣减库存（乐观锁，ge stock）
        boolean ok = dishService.update()
                .setSql("stock = stock - " + request.getQuantity())
                .eq("id", dish.getId())
                .ge("stock", request.getQuantity())
                .update();

        if (!ok) return false;

        // 3. 创建订单对象
        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setAddress(user.getAddress());
        order.setDishId(request.getDishId());
        order.setDishName(dish.getName());
        order.setQuantity(request.getQuantity());

        BigDecimal amount = dish.getPrice().multiply(new BigDecimal(request.getQuantity()));
        BigDecimal costPrice = (dish.getCost() != null) ? dish.getCost() : dish.getPrice().multiply(new BigDecimal("0.6"));
        BigDecimal totalCost = costPrice.multiply(new BigDecimal(request.getQuantity()));

        order.setTotalAmount(amount);
        order.setTotalCost(totalCost);
        order.setDeliveryType(request.getDeliveryType());
        order.setDeliveryTime(request.getDeliveryTime());
        order.setStatus("1");
        order.setOrderTime(LocalDateTime.now());

        return this.save(order);
    }

    /**
     * 计算图表核心数据：总营收、总单数、总利润
     */
    @Override
    public Map<String, Object> getStatistics(String range) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        String dateFormat;

        switch (range) {
            case "week":
                startTime = now.minusDays(7);
                dateFormat = "%m-%d";
                break;
            case "month":
                startTime = now.minusMonths(1);
                dateFormat = "%m-%d";
                break;
            case "year":
                startTime = now.minusYears(1);
                dateFormat = "%Y-%m";
                break;
            default:
                startTime = now.with(LocalTime.MIN);
                dateFormat = "%H:00";
                break;
        }

        Map<String, Object> overview = this.baseMapper.getStatisticsOverview(startTime);
        List<Map<String, Object>> chart = this.baseMapper.getStatisticsChart(startTime, dateFormat);

        result.put("overview", overview);
        result.put("chart", chart);
        return result;
    }

    @Override
    public Map<String, Object> getTodayOverview() {
        String today = LocalDate.now().toString();
        Map<String, Object> stats = this.baseMapper.getTodayStats(today);

        BigDecimal revenue = new BigDecimal(stats.get("revenue").toString());
        BigDecimal profit = revenue.multiply(new BigDecimal("0.4"));

        Map<String, Object> overview = new HashMap<>();
        overview.put("revenue", revenue);
        overview.put("profit", profit.setScale(2, RoundingMode.HALF_UP));
        overview.put("orderCount", stats.get("orderCount"));

        return overview;
    }

    /**
     * 近7天趋势：按日期分组统计营业额
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