package com.smartshop.controller;

import com.smartshop.service.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SecurityUtils securityUtils;

    @GetMapping("/")
    public String home() {
        if (securityUtils.isAdmin()) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/shop";
    }

    /**
     * Precache target and fallback for the service worker. It must stay behind a
     * permitAll rule so it can render without a connection or a session.
     */
    @GetMapping("/offline")
    public String offline() {
        return "offline";
    }
}