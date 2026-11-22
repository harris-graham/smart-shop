package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Integer orderid;
    private String orderNumber;
    private String customerName;
    private String employeeName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private String couponCode;
    private String status;
    private String paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String notes;
    private List<OrderItemDTO> items;
}
