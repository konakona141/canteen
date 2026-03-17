package com.example.canteen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体类 - 对应数据库 user 表
 */
@Data
@TableName("user") // 指定对应的数据库表名
public class User {

    @TableId(type = IdType.AUTO) // 指定主键自增
    private Long id;

    private String username;

    private String password;

    private String role; // 角色：USER(业主) 或 ADMIN(管理员)
    private String address;

}