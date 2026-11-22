package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPerformance {
    private Integer custid;
    private String custname;
    private long orderCount;
    private BigDecimal revenue;
}
