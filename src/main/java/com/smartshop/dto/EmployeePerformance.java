package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePerformance {
    private Integer empid;
    private String empname;
    private long quantitySold;
    private BigDecimal revenue;
}
