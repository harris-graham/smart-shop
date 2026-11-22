package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Integer cartitemid;
    private String smartid;
    private String smartname;
    private String smartimg;
    private Integer quantity;
    private BigDecimal priceAtAdd;
    private BigDecimal subtotal;
}
