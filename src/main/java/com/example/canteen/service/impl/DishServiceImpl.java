package com.example.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.canteen.entity.Dish;
import com.example.canteen.mapper.DishMapper;
import com.example.canteen.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
}
