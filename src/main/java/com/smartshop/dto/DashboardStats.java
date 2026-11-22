package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long productCount;
    private long customerCount;
    private long employeeCount;
    private long orderCount;
    private long pendingOrders;
    private long pendingReviews;
    private long lowStockCount;
    private BigDecimal totalRevenue;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private List<TopProduct> topProducts;
    private List<EmployeePerformance> employeePerformance;
    private List<CustomerPerformance> topCustomers;
    private List<DailySales> lastSevenDays;
}
