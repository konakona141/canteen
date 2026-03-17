package com.example.canteen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.canteen.common.Result;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Order;
import com.example.canteen.service.DishService;
import com.example.canteen.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/canteen/admin")
public class AdminController {

    @Autowired private DishService dishService;
    @Autowired private OrderService orderService;

    /** 经营大屏数据 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData(@RequestParam(defaultValue = "day") String range) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> statistics = orderService.getStatistics(range);
        data.put("overview", statistics.get("overview"));
        data.put("chart", statistics.get("chart"));
        data.put("latestOrders", orderService.list(
                new QueryWrapper<Order>().orderByDesc("order_time").last("limit 5")));
        return Result.success(data);
    }

    /** 更新订单状态 */
    @PutMapping("/order/status/{id}")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.update().set("status", status).eq("id", id).update();
        return Result.success();
    }

    /** 菜品列表（自动过滤 is_deleted=1） */
    @GetMapping("/dish/all")
    public Result<List<Dish>> getAllDishes() {
        return Result.success(dishService.list());
    }

    /** 新增菜品 */
    @PostMapping("/dish/add")
    public Result<Void> addDish(@RequestBody Dish dish) {
        dishService.save(dish);
        return Result.success();
    }

    /** 更新菜品（包括 saleType） */
    @PutMapping("/dish/update")
    public Result<Void> updateDish(@RequestBody Dish dish) {
        dishService.updateById(dish);
        return Result.success();
    }

    /** 切换上下架状态 */
    @PutMapping("/dish/status/{id}/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        dishService.update().set("status", status).eq("id", id).update();
        return Result.success();
    }

    /** 逻辑删除 */
    @DeleteMapping("/dish/{id}")
    public Result<Void> deleteDish(@PathVariable Long id) {
        dishService.removeById(id);
        return Result.success();
    }

    /** 兼容旧接口 save（saveOrUpdate） */
    @PostMapping("/dish/save")
    public Result<Void> save(@RequestBody Dish dish) {
        dishService.saveOrUpdate(dish);
        return Result.success();
    }
}
