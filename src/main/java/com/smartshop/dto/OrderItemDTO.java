package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Integer orderitemid;
    private String smartid;
    private String smartname;
    private String smartimg;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
