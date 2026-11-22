package com.smartshop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Keeps the URLs used by the legacy JSP application working after the migration.
 * The old screens are replaced by the Spring MVC pages.
 */
@Controller
public class LegacyRedirectController {

    @GetMapping("/smartLogin")
    public String login() {
        return "redirect:/login";
    }

    @GetMapping("/smartLogout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login?logout";
    }

    @GetMapping("/smartShop")
    public String shop() {
        return "redirect:/shop";
    }

    @GetMapping("/smartSelect")
    public String smartphones() {
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/empSelect")
    public String employees() {
        return "redirect:/admin/employees";
    }

    @GetMapping("/custSelect")
    public String customers() {
        return "redirect:/admin/customers";
    }

    @GetMapping("/saleSelect")
    public String sales() {
        return "redirect:/admin/sales";
    }

    @GetMapping("/addToCart")
    public String addToCart() {
        return "redirect:/shop";
    }

    @GetMapping("/remFromCart")
    public String removeFromCart() {
        return "redirect:/cart";
    }

    @GetMapping("/smartSale")
    public String sale() {
        return "redirect:/cart";
    }

    @GetMapping("/smartMore")
    public String restock() {
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/smartInsert")
    public String insertSmartphone() {
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/smartUpdate")
    public String updateSmartphone() {
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/smartDelete")
    public String deleteSmartphone() {
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/empInsert")
    public String insertEmployee() {
        return "redirect:/admin/employees";
    }

    @GetMapping("/empUpdate")
    public String updateEmployee() {
        return "redirect:/admin/employees";
    }

    @GetMapping("/empAccUpdate")
    public String updateAccount() {
        return "redirect:/account";
    }

    @GetMapping("/empDelete")
    public String deleteEmployee() {
        return "redirect:/admin/employees";
    }

    @GetMapping("/custInsert")
    public String insertCustomer() {
        return "redirect:/admin/customers";
    }

    @GetMapping("/custQuiInsert")
    public String quickInsertCustomer() {
        return "redirect:/admin/customers";
    }

    @GetMapping("/custUpdate")
    public String updateCustomer() {
        return "redirect:/admin/customers";
    }

    @GetMapping("/custDelete")
    public String deleteCustomer() {
        return "redirect:/admin/customers";
    }
}
