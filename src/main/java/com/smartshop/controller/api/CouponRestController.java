package com.smartshop.controller.api;

import com.smartshop.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Promotion codes applied at checkout")
public class CouponRestController {

    private final CouponService couponService;

    @GetMapping("/validate")
    @Operation(summary = "Validate a coupon code against a cart subtotal")
    public Map<String, Object> validate(@RequestParam String code,
                                        @RequestParam(defaultValue = "0") BigDecimal amount) {
        CouponService.Discount discount = couponService.validate(code, amount);
        return Map.of(
                "code", discount.coupon().getCode(),
                "description", discount.coupon().getDescription() == null
                        ? "" : discount.coupon().getDescription(),
                "discountType", discount.coupon().getDiscountType(),
                "discountValue", discount.coupon().getDiscountValue(),
                "discountAmount", discount.amount(),
                "totalAfterDiscount", amount.subtract(discount.amount()),
                "valid", true);
    }
}