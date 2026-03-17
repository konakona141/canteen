package com.example.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    // MP 自动注入了 selectById, update, insert 等方法
}

