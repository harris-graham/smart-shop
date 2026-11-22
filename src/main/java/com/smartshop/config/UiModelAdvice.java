package com.smartshop.config;

import com.smartshop.service.CartService;
import com.smartshop.service.SecurityUtils;
import com.smartshop.service.ThemeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Supplies the values every Thymeleaf layout fragment needs, so individual
 * controllers do not have to repeat them.
 */
@ControllerAdvice(basePackages = "com.smartshop.controller")
@RequiredArgsConstructor
public class UiModelAdvice {

    private final ThemeService themeService;
    private final SecurityUtils securityUtils;
    private final CartService cartService;

    @ModelAttribute("theme")
    public String theme(HttpServletRequest request) {
        String fromCookie = themeService.resolve(request);
        if (fromCookie != null) {
            return fromCookie;
        }
        return themeService.preferred(securityUtils.currentUsername().orElse(null));
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        return securityUtils.currentUsername().map(cartService::itemCount).orElse(0);
    }
}