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
     * ??????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean placeOrder(OrderRequest request) {
        // 1. ???????????
        User user = userService.getById(request.getUserId());
        Dish dish = dishService.getById(request.getDishId());

        // ?????????????????
        if (dish == null || dish.getStock() < request.getQuantity()) {
            throw new BusinessException("??????????");
        }

        // ???????????
        if (request.getDeliveryType() == null || request.getDeliveryType().isBlank()) {
            throw new BusinessException("????????????????");
        }

        // ?????????????/?????
        if (Integer.valueOf(1).equals(dish.getSaleType())) {
            if (request.getDeliveryTime() == null || request.getDeliveryTime().isBlank()) {
                throw new BusinessException("???????????????");
            }
        }

        // 2. ??????????????? ge stock?
        boolean ok = dishService.update()
                .setSql("stock = stock - " + request.getQuantity())
                .eq("id", dish.getId())
                .ge("stock", request.getQuantity())
                .update();

        if (!ok) return false;

        // 3. ?????????????
        Order order = new Order();

        // ??????
        order.setUserId(request.getUserId());
        order.setAddress(user.getAddress()); // ????????

        // ????????
        order.setDishId(request.getDishId());
        order.setDishName(dish.getName());
        order.setQuantity(request.getQuantity());

        // ???????
        BigDecimal amount = dish.getPrice().multiply(new BigDecimal(request.getQuantity()));
        BigDecimal costPrice = (dish.getCost() != null) ? dish.getCost() : dish.getPrice().multiply(new BigDecimal("0.6"));
        BigDecimal totalCost = costPrice.multiply(new BigDecimal(request.getQuantity()));

        order.setTotalAmount(amount);
        order.setTotalCost(totalCost);

        // ????????
        order.setDeliveryType(request.getDeliveryType());
        order.setDeliveryTime(request.getDeliveryTime());

        // ?????????
        order.setStatus("1"); // "1" ?????
        order.setOrderTime(LocalDateTime.now());

        // 4. ????
        return this.save(order);
    }

    /**
     * ????????????????????
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
            default: // day
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
        BigDecimal profit = revenue.multiply(new BigDecimal("0.4")); // ????40%

        Map<String, Object> overview = new HashMap<>();
        overview.put("revenue", revenue);
        overview.put("profit", profit.setScale(2, RoundingMode.HALF_UP));
        overview.put("orderCount", stats.get("orderCount"));

        return overview;
    }

    /**
     * ?7??????????????
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
