package com.example.canteen.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.canteen.common.Result;
import com.example.canteen.entity.Dish;
import com.example.canteen.service.DishService;
import com.example.canteen.service.OrderService;
import com.example.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.canteen.dto.OrderRequest;
import com.example.canteen.entity.Order;
import com.example.canteen.entity.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/canteen")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private DishService dishService;

    // --- 用户操作 ---

    /**
     * 菜单接口：saleType=0 现售，saleType=1 预售，只返回 status=1 且未删除的菜品
     */
    @GetMapping("/menu")
    public Result<Map<String, List<Dish>>> getMenu() {
        List<Dish> dishes = dishService.lambdaQuery().eq(Dish::getStatus, 1).list();
        Map<String, List<Dish>> menu = new HashMap<>();
        menu.put("current",  dishes.stream().filter(d -> Integer.valueOf(0).equals(d.getSaleType())).collect(Collectors.toList()));
        menu.put("presale", dishes.stream().filter(d -> Integer.valueOf(1).equals(d.getSaleType())).collect(Collectors.toList()));
        return Result.success(menu);
    }

    /** 下单 */
    @PostMapping("/order")
    public Result<String> createOrder(@RequestBody OrderRequest request) {
        boolean ok = orderService.placeOrder(request);
        return ok ? Result.success("下单成功") : Result.error("库存不足");
    }

    /** 取消订单（回补库存） */
    @PostMapping("/order/cancel")
    public Result<String> cancelOrder(@RequestParam Long orderId, @RequestParam Long userId) {
        Order order = orderService.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!order.getUserId().equals(userId)) return Result.error("无权操作");
        if (!"1".equals(order.getStatus())) return Result.error("该订单状态无法取消");
        dishService.update()
                .setSql("stock = stock + " + order.getQuantity())
                .eq("id", order.getDishId()).update();
        orderService.update()
                .set("status", "3")
                .set("cancel_time", LocalDateTime.now())
                .eq("id", orderId).update();
        return Result.success("取消成功");
    }
    /**
     * 用户：查看自己的订单列表
     */
    @GetMapping("/my-orders")
    public Result<List<Order>> getMyOrders(@RequestParam Long userId) {
        return Result.success(orderService.lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getOrderTime)
                .list());
    }

    /**
     * 用户：更新个人配送地址
     */
    @PostMapping("/user/update")
    public Result<Void> updateAddress(@RequestBody User user) {
        userService.update().set("address", user.getAddress())
                .eq("id", user.getId()).update();
        return Result.success();
    }

    // --- 管理员操作 ---

    /**
     * 管理端：获取所有订单明细
     */
    @GetMapping("/admin/orders")
    public Result<List<Order>> getAllOrders() {
        // 这里建议返回包含用户信息的订单列表
        return Result.success(orderService.list(new QueryWrapper<Order>().orderByDesc("order_time")));
    }

    /**
     * 管理端：处理订单 (如点击"出餐")
     */
    @PutMapping("/admin/order/status/{id}/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        orderService.update().set("status", status).eq("id", id).update();
        return Result.success();
    }
}
