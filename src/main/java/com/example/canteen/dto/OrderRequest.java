package com.example.canteen.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderRequest {
    private Long userId;
    private Long dishId;
    private Integer quantity;
    private BigDecimal totalPrice;

}