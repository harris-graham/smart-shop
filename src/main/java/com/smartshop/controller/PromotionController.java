package com.smartshop.controller;

import com.smartshop.dto.CouponForm;
import com.smartshop.entity.Coupon;
import com.smartshop.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final CouponService couponService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coupons", couponService.findAll());
        if (!model.containsAttribute("couponForm")) {
            model.addAttribute("couponForm", new CouponForm());
        }
        return "admin/promotions";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("couponForm") CouponForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the coupon fields");
            return "redirect:/admin/promotions";
        }
        try {
            couponService.create(form.getCode(), form.getDescription(), form.getDiscountType(),
                    form.getDiscountValue(), form.getMinOrderAmount(),
                    !Boolean.FALSE.equals(form.getActive()), parseDate(form.getExpiryDate()));
            redirectAttributes.addFlashAttribute("message", "Coupon created");
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/{couponid}/toggle")
    public String toggle(@PathVariable Integer couponid,
                         @RequestParam boolean active,
                         RedirectAttributes redirectAttributes) {
        couponService.setActive(couponid, active);
        redirectAttributes.addFlashAttribute("message", active ? "Coupon enabled" : "Coupon disabled");
        return "redirect:/admin/promotions";
    }

    @PostMapping("/{couponid}/delete")
    public String delete(@PathVariable Integer couponid, RedirectAttributes redirectAttributes) {
        couponService.delete(couponid);
        redirectAttributes.addFlashAttribute("message", "Coupon deleted");
        return "redirect:/admin/promotions";
    }

    @GetMapping("/{couponid}/edit")
    public String edit(@PathVariable Integer couponid, Model model,
                       RedirectAttributes redirectAttributes) {
        Coupon coupon = couponService.findAll().stream()
                .filter(candidate -> couponid.equals(candidate.getCouponid()))
                .findFirst()
                .orElse(null);
        if (coupon == null) {
            redirectAttributes.addFlashAttribute("error", "Coupon not found");
            return "redirect:/admin/promotions";
        }
        CouponForm form = new CouponForm();
        form.setCouponid(coupon.getCouponid());
        form.setCode(coupon.getCode());
        form.setDescription(coupon.getDescription());
        form.setDiscountType(coupon.getDiscountType());
        form.setDiscountValue(coupon.getDiscountValue());
        form.setMinOrderAmount(coupon.getMinOrderAmount());
        form.setActive(coupon.isActive());
        form.setExpiryDate(coupon.getExpiryDate() == null ? null : coupon.getExpiryDate().toString());
        model.addAttribute("couponForm", form);
        return list(model);
    }

    @PostMapping("/{couponid}")
    public String update(@PathVariable Integer couponid,
                         @Valid @ModelAttribute("couponForm") CouponForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the coupon fields");
            return "redirect:/admin/promotions";
        }
        try {
            couponService.update(couponid, form.getDescription(), form.getDiscountType(),
                    form.getDiscountValue(), form.getMinOrderAmount(),
                    !Boolean.FALSE.equals(form.getActive()), parseDate(form.getExpiryDate()));
            redirectAttributes.addFlashAttribute("message", "Coupon updated");
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}