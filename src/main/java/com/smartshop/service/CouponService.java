package com.smartshop.service;

import com.smartshop.entity.Coupon;
import com.smartshop.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public List<Coupon> findAll() {
        return couponRepository.findAllByOrderByCouponidDesc();
    }

    @Transactional(readOnly = true)
    public Coupon findByCode(String code) {
        String normalised = normalise(code);
        if (normalised == null) {
            throw new IllegalArgumentException("Enter a coupon code");
        }
        return couponRepository.findByCodeIgnoreCase(normalised)
                .orElseThrow(() -> new IllegalArgumentException("Unknown coupon code"));
    }

    /**
     * Validates a code against the cart subtotal and returns the discount to
     * apply. The coupon is never persisted here; usage is counted at checkout.
     */
    @Transactional(readOnly = true)
    public Discount validate(String code, BigDecimal subtotal) {
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        Coupon coupon = findByCode(code);
        if (!coupon.isActive()) {
            throw new IllegalArgumentException("This coupon is no longer active");
        }
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("This coupon has expired");
        }
        BigDecimal minimum = coupon.getMinOrderAmount() == null
                ? BigDecimal.ZERO
                : coupon.getMinOrderAmount();
        if (subtotal.compareTo(minimum) < 0) {
            throw new IllegalArgumentException("This coupon requires a minimum spend of "
                    + minimum.stripTrailingZeros().toPlainString());
        }
        return new Discount(coupon, discountFor(coupon, subtotal));
    }

    @Transactional
    public void recordUsage(Integer couponid) {
        if (couponid == null) {
            return;
        }
        couponRepository.findById(couponid).ifPresent(coupon -> {
            coupon.setUsageCount((coupon.getUsageCount() == null ? 0 : coupon.getUsageCount()) + 1);
            couponRepository.save(coupon);
        });
    }

    @Transactional
    public Coupon create(String code, String description, String discountType,
                         BigDecimal discountValue, BigDecimal minOrderAmount,
                         boolean active, LocalDate expiryDate) {
        String normalised = normalise(code);
        if (normalised == null) {
            throw new IllegalArgumentException("A coupon code is required");
        }
        if (couponRepository.existsByCodeIgnoreCase(normalised)) {
            throw new IllegalArgumentException("That coupon code already exists");
        }
        String type = "FIXED".equalsIgnoreCase(discountType) ? "FIXED" : "PERCENT";
        if (discountValue == null || discountValue.signum() < 0) {
            throw new IllegalArgumentException("Enter a discount greater than zero");
        }
        if ("PERCENT".equals(type) && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("A percentage discount cannot exceed 100");
        }
        Coupon coupon = new Coupon(normalised, description, type, discountValue,
                minOrderAmount == null || minOrderAmount.signum() < 0 ? BigDecimal.ZERO : minOrderAmount,
                active, expiryDate);
        coupon.setUsageCount(0);
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon update(Integer couponid, String description, String discountType,
                         BigDecimal discountValue, BigDecimal minOrderAmount,
                         boolean active, LocalDate expiryDate) {
        Coupon coupon = couponRepository.findById(couponid)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        String type = "FIXED".equalsIgnoreCase(discountType) ? "FIXED" : "PERCENT";
        if (discountValue == null || discountValue.signum() < 0) {
            throw new IllegalArgumentException("Enter a discount greater than zero");
        }
        if ("PERCENT".equals(type) && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("A percentage discount cannot exceed 100");
        }
        coupon.setDescription(description);
        coupon.setDiscountType(type);
        coupon.setDiscountValue(discountValue);
        coupon.setMinOrderAmount(minOrderAmount == null || minOrderAmount.signum() < 0
                ? BigDecimal.ZERO
                : minOrderAmount);
        coupon.setActive(active);
        coupon.setExpiryDate(expiryDate);
        return couponRepository.save(coupon);
    }

    @Transactional
    public void setActive(Integer couponid, boolean active) {
        couponRepository.findById(couponid).ifPresent(coupon -> {
            coupon.setActive(active);
            couponRepository.save(coupon);
        });
    }

    @Transactional
    public void delete(Integer couponid) {
        couponRepository.deleteById(couponid);
    }

    private BigDecimal discountFor(Coupon coupon, BigDecimal subtotal) {
        BigDecimal value = coupon.getDiscountValue() == null
                ? BigDecimal.ZERO
                : coupon.getDiscountValue();
        BigDecimal discount = "FIXED".equalsIgnoreCase(coupon.getDiscountType())
                ? value
                : subtotal.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (discount.compareTo(subtotal) > 0) {
            return subtotal;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalise(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim().toUpperCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record Discount(Coupon coupon, BigDecimal amount) {
    }
}