package com.example.canteen.entity;

import java.math.BigDecimal;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 菜品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dishId;

    /** 冗余菜名：防止菜品删了订单变空白 */
    private String dishName;

    /** 份数 */
    private Integer quantity;

    /** 总售价 */
    private BigDecimal totalAmount;

    /** 总成本：下单时计算并存入 */
    private BigDecimal totalCost;

    /** 状态 (PENDING, PAID, etc.) */
    private String status;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 收货地址 */
    private String address;
    /** 取消时间 */
    private LocalDateTime cancelTime;


}