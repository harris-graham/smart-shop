package com.smartshop.service;

import com.smartshop.entity.Coupon;
import com.smartshop.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void percentageCouponIsCalculatedFromTheSubtotal() {
        couponRepository.save(any(Coupon.class)).thenAnswer(invocation -> invocation.getArgument(0));
        Coupon saved = couponService.create("welcome10", "Ten percent off", "PERCENT",
                new BigDecimal("10"), BigDecimal.ZERO, true, null);
        when(couponRepository.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(saved));

        CouponService.Discount discount = couponService.validate(" welcome10 ",
                new BigDecimal("200.00"));

        assertThat(discount.coupon().getCode()).isEqualTo("WELCOME10");
        assertThat(discount.amount()).isEqualByComparingTo("20.00");
    }

    @Test
    void fixedCouponIsCappedAtTheSubtotal() {
        Coupon coupon = new Coupon();
        coupon.setCouponid(1);
        coupon.setCode("FLAT50");
        coupon.setDiscountType("FIXED");
        coupon.setDiscountValue(new BigDecimal("50"));
        coupon.setActive(true);
        when(couponRepository.findByCodeIgnoreCase("FLAT50")).thenReturn(Optional.of(coupon));

        CouponService.Discount discount = couponService.validate("FLAT50", new BigDecimal("30.00"));

        assertThat(discount.amount()).isEqualByComparingTo("30.00");
    }

    @Test
    void rejectsInactiveExpiredAndBelowMinimumCoupons() {
        Coupon inactive = coupon();
        inactive.setActive(false);
        when(couponRepository.findByCodeIgnoreCase("OFF")).thenReturn(Optional.of(inactive));

        Coupon expired = coupon();
        expired.setCode("OLD");
        expired.setExpiryDate(LocalDate.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("OLD")).thenReturn(Optional.of(expired));

        Coupon withMinimum = coupon();
        withMinimum.setCode("MIN");
        withMinimum.setMinOrderAmount(new BigDecimal("500.00"));
        when(couponRepository.findByCodeIgnoreCase("MIN")).thenReturn(Optional.of(withMinimum));

        assertThatThrownBy(() -> couponService.validate("OFF", BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no longer active");
        assertThatThrownBy(() -> couponService.validate("OLD", BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
        assertThatThrownBy(() -> couponService.validate("MIN", new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum spend");
    }

    @Test
    void createRejectsDuplicateCodesAndInvalidPercentages() {
        when(couponRepository.existsByCodeIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> couponService.create("WELCOME", null, "PERCENT",
                new BigDecimal("10"), BigDecimal.ZERO, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(couponRepository, never()).save(any(Coupon.class));
    }

    private Coupon coupon() {
        Coupon coupon = new Coupon();
        coupon.setCouponid(1);
        coupon.setCode("SAVE");
        coupon.setDiscountType("PERCENT");
        coupon.setDiscountValue(new BigDecimal("10"));
        coupon.setActive(true);
        return coupon;
    }
}