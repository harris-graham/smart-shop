package com.smartshop.controller.api;

import com.smartshop.dto.OrderDTO;
import com.smartshop.dto.StatusRequest;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Order;
import com.smartshop.service.OrderService;
import com.smartshop.service.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public List<OrderDTO> list() {
        Optional<Customer> customer = securityUtils.currentCustomer();
        List<Order> orders = customer
                .map(value -> orderService.findByCustomer(value.getCustid()))
                .orElseGet(orderService::findAll);
        return orders.stream().map(orderService::toDTO).toList();
    }

    @GetMapping("/{orderid}")
    public OrderDTO get(@PathVariable Integer orderid) {
        return orderService.toDTO(ownedOrder(orderid));
    }

    @PostMapping("/{orderid}/cancel")
    public OrderDTO cancel(@PathVariable Integer orderid) {
        ownedOrder(orderid);
        return orderService.toDTO(orderService.cancel(orderid));
    }

    @PutMapping("/{orderid}/status")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public OrderDTO updateStatus(@PathVariable Integer orderid,
                                 @Valid @RequestBody StatusRequest request) {
        return orderService.toDTO(orderService.updateStatus(orderid, request.getStatus()));
    }

    private Order ownedOrder(Integer orderid) {
        Order order = orderService.findById(orderid);
        Optional<Customer> customer = securityUtils.currentCustomer();
        if (customer.isPresent() && (order.getCustomer() == null
                || !customer.get().getCustid().equals(order.getCustomer().getCustid()))) {
            throw new AccessDeniedException("This order belongs to another customer");
        }
        return order;
    }
}
