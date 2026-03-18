package com.example.canteen.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderRequest {
    private Long userId;
    private Long dishId;
    private Integer quantity;
    private BigDecimal totalPrice;

    /**
     * 配送方式：delivery=配送上门，pickup=自取
     */
    private String deliveryType;

    /**
     * 预售商品的配送/取货时间段，格式如 "2026-03-19 11:00-12:00"
     */
    private String deliveryTime;

}