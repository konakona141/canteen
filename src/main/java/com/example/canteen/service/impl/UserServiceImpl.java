package com.example.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.canteen.entity.User;
import com.example.canteen.mapper.UserMapper;
import com.example.canteen.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
