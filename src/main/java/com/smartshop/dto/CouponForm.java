package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponForm {

    private Integer couponid;

    private String code;

    private String description;

    private String discountType = "PERCENT";

    private BigDecimal discountValue;

    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    private Boolean active = true;

    private String expiryDate;
}