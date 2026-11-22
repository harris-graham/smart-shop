package com.smartshop.controller.api;

import com.smartshop.entity.Customer;
import com.smartshop.entity.Smartphone;
import com.smartshop.service.SecurityUtils;
import com.smartshop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistRestController {

    private final WishlistService wishlistService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public List<Smartphone> list() {
        Customer customer = customer();
        return wishlistService.findByCustomer(customer.getCustid()).stream()
                .map(wishlist -> wishlist.getSmartphone())
                .toList();
    }

    @PostMapping("/{smartid}")
    public void add(@PathVariable String smartid) {
        wishlistService.add(customer().getCustid(), smartid);
    }

    @DeleteMapping("/{smartid}")
    public void remove(@PathVariable String smartid) {
        wishlistService.remove(customer().getCustid(), smartid);
    }

    private Customer customer() {
        return securityUtils.currentCustomer()
                .orElseThrow(() -> new IllegalStateException("Only customers have a wishlist"));
    }
}
