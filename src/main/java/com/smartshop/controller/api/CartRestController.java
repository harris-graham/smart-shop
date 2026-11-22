package com.smartshop.controller.api;

import com.smartshop.dto.AddToCartRequest;
import com.smartshop.dto.CartDTO;
import com.smartshop.dto.CheckoutForm;
import com.smartshop.dto.OrderDTO;
import com.smartshop.dto.UpdateCartRequest;
import com.smartshop.entity.Cart;
import com.smartshop.entity.Order;
import com.smartshop.service.CartService;
import com.smartshop.service.OrderService;
import com.smartshop.service.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartRestController {

    private final CartService cartService;
    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public CartDTO getCart() {
        return cartService.getCartDTO(username());
    }

    @GetMapping("/count")
    public int count() {
        return cartService.itemCount(username());
    }

    @PostMapping("/items")
    public CartDTO addItem(@Valid @RequestBody AddToCartRequest request) {
        cartService.addItem(username(), request.getSmartid(), request.getQuantity());
        return cartService.getCartDTO(username());
    }

    @PutMapping("/items/{itemId}")
    public CartDTO updateItem(@PathVariable Integer itemId,
                              @Valid @RequestBody UpdateCartRequest request) {
        cartService.updateQuantity(username(), itemId, request.getQuantity());
        return cartService.getCartDTO(username());
    }

    @DeleteMapping("/items/{itemId}")
    public CartDTO removeItem(@PathVariable Integer itemId) {
        cartService.removeItem(username(), itemId);
        return cartService.getCartDTO(username());
    }

    @DeleteMapping
    public void clear() {
        cartService.clear(username());
    }

    @PostMapping("/checkout")
    public OrderDTO checkout(@RequestBody(required = false) CheckoutForm form) {
        Order order = orderService.createFromCart(username(), form == null ? new CheckoutForm() : form);
        return orderService.toDTO(order);
    }

    private String username() {
        return securityUtils.currentUsername().orElseThrow();
    }
}
