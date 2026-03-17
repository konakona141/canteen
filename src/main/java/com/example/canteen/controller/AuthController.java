package com.example.canteen.controller;
import com.example.canteen.common.Result;
import com.example.canteen.service.DishService;
import com.example.canteen.service.OrderService;
import com.example.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.canteen.entity.Dish;
import com.example.canteen.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canteen")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private DishService dishService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        if (userService.lambdaQuery().eq(User::getUsername, user.getUsername()).one() != null) {
            return Result.error("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userService.save(user) ? Result.success(null) : Result.error("注册失败");
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody User loginUser) {
        User user = userService.lambdaQuery().eq(User::getUsername, loginUser.getUsername()).one();
        if (user != null && passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            user.setPassword(null); // 巧思：不要把加密后的密码传回前端
            return Result.success(user);
        }
        return Result.error("用户名或密码错误");
    }

}
