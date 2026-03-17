package com.example.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.canteen.entity.User;
import com.example.canteen.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {

}