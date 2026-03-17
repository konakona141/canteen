package com.example.canteen.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderAdminView {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private LocalDateTime orderTime;

    private String username;

    private String address;

    private String dishName;

    private Integer quantity;

    private BigDecimal totalAmount;

    /** 订单状态：与前端保持兼容（目前为 "1"/"2"） */
    private String status;
}

