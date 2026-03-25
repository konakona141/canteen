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

    /** 状态 */
    private String status;

    /** 下单时间 */
    private LocalDateTime orderTime;

    private String address;

    /** 配送方式：delivery=配送上门，pickup=自取 */
    private String deliveryType;

    /** 预售商品的配送/取货时间段，格式如 2026-03-19 11:00-12:00 */
    private String deliveryTime;

    /** 管理端取消订单时填写的取消理由 */
    private String cancelReason;

}