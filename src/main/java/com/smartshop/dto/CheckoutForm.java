package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutForm {

    /** Required when an employee checks out on behalf of a customer. */
    private Integer customerId;

    /** Optional promotion code applied to the order. */
    private String couponCode;

    private String notes;
}
