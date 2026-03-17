package com.example.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.canteen.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}