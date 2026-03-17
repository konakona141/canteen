package com.example.canteen.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data 
@TableName("dish")
public class Dish {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String category; 
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stock;
    private String imageUrl;
    private String description;
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    private Integer saleType;
}