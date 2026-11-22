package com.smartshop.controller;

import com.smartshop.dto.CartDTO;
import com.smartshop.dto.CheckoutForm;
import com.smartshop.dto.ReviewForm;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Order;
import com.smartshop.entity.Smartphone;
import com.smartshop.entity.Wishlist;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.service.CartService;
import com.smartshop.service.CustomerService;
import com.smartshop.service.OrderService;
import com.smartshop.service.ReviewService;
import com.smartshop.service.SecurityUtils;
import com.smartshop.service.SmartphoneService;
import com.smartshop.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final SmartphoneService smartphoneService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final CustomerService customerService;
    private final SecurityUtils securityUtils;

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) Integer minRam,
                       @RequestParam(required = false) Integer minStorage,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       Model model) {
        Page<Smartphone> products = smartphoneService.search(keyword, minPrice, maxPrice,
                minRam, minStorage, PageRequest.of(page, size, Sort.by("smartname").ascending()));
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minRam", minRam);
        model.addAttribute("minStorage", minStorage);
        model.addAttribute("cartCount", cartCount());
        model.addAttribute("isCustomer", securityUtils.isCustomer());
        model.addAttribute("isEmployee", securityUtils.isEmployee());
        return "shop/index";
    }

    @GetMapping("/shop/{smartid}")
    public String product(@PathVariable String smartid, Model model) {
        Smartphone smartphone = smartphoneService.findById(smartid);
        model.addAttribute("product", smartphone);
        model.addAttribute("reviews", reviewService.approvedForProduct(smartid));
        model.addAttribute("averageRating", reviewService.averageRating(smartid));
        model.addAttribute("reviewCount", reviewService.approvedCount(smartid));
        model.addAttribute("reviewForm", new ReviewForm(smartid, 5, ""));
        model.addAttribute("isCustomer", securityUtils.isCustomer());
        model.addAttribute("wishlisted", securityUtils.currentCustomer()
                .map(customer -> wishlistService.exists(customer.getCustid(), smartid))
                .orElse(false));
        model.addAttribute("cartCount", cartCount());
        return "shop/detail";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        String username = securityUtils.currentUsername().orElseThrow();
        CartDTO cart = cartService.getCartDTO(username);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", new CheckoutForm());
        model.addAttribute("cartCount", cart.getItemCount());
        if (securityUtils.isEmployee()) {
            model.addAttribute("customers", customerService.findAll());
        }
        model.addAttribute("isCustomer", securityUtils.isCustomer());
        return "shop/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String smartid,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        String username = securityUtils.currentUsername().orElseThrow();
        try {
            cartService.addItem(username, smartid, quantity);
            redirectAttributes.addFlashAttribute("message", "Added to cart");
        } catch (InsufficientStockException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/shop";
    }

    @PostMapping("/cart/update/{itemId}")
    public String updateCart(@PathVariable Integer itemId,
                             @RequestParam int quantity,
                             RedirectAttributes redirectAttributes) {
        String username = securityUtils.currentUsername().orElseThrow();
        try {
            cartService.updateQuantity(username, itemId, quantity);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{itemId}")
    public String removeFromCart(@PathVariable Integer itemId,
                                 RedirectAttributes redirectAttributes) {
        String username = securityUtils.currentUsername().orElseThrow();
        cartService.removeItem(username, itemId);
        redirectAttributes.addFlashAttribute("message", "Item removed");
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart() {
        String username = securityUtils.currentUsername().orElseThrow();
        cartService.clear(username);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@ModelAttribute CheckoutForm checkoutForm,
                           RedirectAttributes redirectAttributes) {
        String username = securityUtils.currentUsername().orElseThrow();
        try {
            Order order = orderService.createFromCart(username, checkoutForm);
            redirectAttributes.addFlashAttribute("message", "Order placed successfully");
            return "redirect:/orders/" + order.getOrderid();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        Optional<Customer> customer = securityUtils.currentCustomer();
        List<Order> orders = customer
                .map(value -> orderService.findByCustomer(value.getCustid()))
                .orElseGet(orderService::findAll);
        model.addAttribute("orders", orders);
        model.addAttribute("isCustomer", customer.isPresent());
        model.addAttribute("cartCount", cartCount());
        return "shop/orders";
    }

    @GetMapping("/orders/{orderid}")
    public String order(@PathVariable Integer orderid, Model model) {
        Order order = orderService.findById(orderid);
        Optional<Customer> customer = securityUtils.currentCustomer();
        if (customer.isPresent() && (order.getCustomer() == null
                || !customer.get().getCustid().equals(order.getCustomer().getCustid()))) {
            throw new AccessDeniedException("This order belongs to another customer");
        }
        model.addAttribute("order", order);
        model.addAttribute("cartCount", cartCount());
        return "shop/order-detail";
    }

    @PostMapping("/orders/{orderid}/cancel")
    public String cancelOrder(@PathVariable Integer orderid,
                              RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(orderid);
        Optional<Customer> customer = securityUtils.currentCustomer();
        if (customer.isPresent() && (order.getCustomer() == null
                || !customer.get().getCustid().equals(order.getCustomer().getCustid()))) {
            throw new AccessDeniedException("This order belongs to another customer");
        }
        try {
            orderService.cancel(orderid);
            redirectAttributes.addFlashAttribute("message", "Order cancelled");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + orderid;
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model) {
        Optional<Customer> customer = securityUtils.currentCustomer();
        if (customer.isEmpty()) {
            return "redirect:/shop";
        }
        List<Wishlist> items = wishlistService.findByCustomer(customer.get().getCustid());
        model.addAttribute("wishlist", items);
        model.addAttribute("cartCount", cartCount());
        return "shop/wishlist";
    }

    @PostMapping("/wishlist/{smartid}")
    public String addWishlist(@PathVariable String smartid,
                              RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = securityUtils.currentCustomer();
        if (customer.isEmpty()) {
            return "redirect:/login";
        }
        wishlistService.add(customer.get().getCustid(), smartid);
        redirectAttributes.addFlashAttribute("message", "Added to wishlist");
        return "redirect:/shop/" + smartid;
    }

    @PostMapping("/wishlist/{smartid}/remove")
    public String removeWishlist(@PathVariable String smartid,
                                 RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = securityUtils.currentCustomer();
        if (customer.isEmpty()) {
            return "redirect:/login";
        }
        wishlistService.remove(customer.get().getCustid(), smartid);
        redirectAttributes.addFlashAttribute("message", "Removed from wishlist");
        return "redirect:/wishlist";
    }

    @PostMapping("/reviews")
    public String submitReview(@Valid @ModelAttribute ReviewForm reviewForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        String smartid = reviewForm.getSmartid();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please provide a rating between 1 and 5");
            return "redirect:/shop/" + smartid;
        }
        try {
            reviewService.addReview(securityUtils.currentUsername().orElseThrow(), reviewForm);
            redirectAttributes.addFlashAttribute("message",
                    "Thanks! Your review is awaiting moderation.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/shop/" + smartid;
    }

    private int cartCount() {
        return securityUtils.currentUsername().map(cartService::itemCount).orElse(0);
    }
}
