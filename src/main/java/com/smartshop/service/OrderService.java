package com.smartshop.service;

import com.smartshop.dto.CheckoutForm;
import com.smartshop.dto.OrderDTO;
import com.smartshop.dto.OrderItemDTO;
import com.smartshop.dto.PaymentResult;
import com.smartshop.entity.Cart;
import com.smartshop.entity.CartItem;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.entity.Order;
import com.smartshop.entity.OrderItem;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.CartRepository;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import com.smartshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SaleService saleService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final RealtimeService realtimeService;

    @Transactional
    public Order createFromCart(String username, CheckoutForm form) {
        Cart cart = cartRepository.findBySessionid(username)
                .orElseThrow(() -> new IllegalStateException("Your cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Your cart is empty");
        }

        Customer customer = resolveCustomer(username, form);
        Employee employee = employeeRepository.findByEmpunameIgnoreCase(username).orElse(null);

        Order order = new Order();
        order.setOrderNumber("ORD-" + LocalDateTime.now().format(ORDER_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT));
        order.setCustomer(customer);
        order.setEmployee(employee);
        order.setStatus("PENDING");
        order.setPaymentStatus("PENDING");
        order.setNotes(form == null ? null : form.getNotes());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Smartphone smartphone = cartItem.getSmartphone();
            int available = smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty();
            if (available < cartItem.getQuantity()) {
                throw new InsufficientStockException(smartphone.getSmartname(),
                        cartItem.getQuantity(), available);
            }
            BigDecimal unitPrice = cartItem.getPriceAtAdd() == null
                    ? smartphone.getSmartprice()
                    : cartItem.getPriceAtAdd();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            order.addItem(new OrderItem(order, smartphone, cartItem.getQuantity(), unitPrice, lineTotal));
            subtotal = subtotal.add(lineTotal);
        }

        CouponService.Discount applied = null;
        if (form != null && form.getCouponCode() != null && !form.getCouponCode().isBlank()) {
            applied = couponService.validate(form.getCouponCode(), subtotal);
        }
        BigDecimal discount = applied == null ? BigDecimal.ZERO : applied.amount();
        if (applied != null) {
            order.setCouponCode(applied.coupon().getCode());
        }
        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal.subtract(discount));
        Order saved = orderRepository.save(order);
        if (applied != null) {
            couponService.recordUsage(applied.coupon().getCouponid());
        }

        PaymentResult payment = paymentService.process(saved);
        if (payment.isSuccessful()) {
            saved.setPaymentStatus("PAID");
            saved.setPaymentId(payment.getTransactionId());
        } else {
            saved.setPaymentStatus("FAILED");
        }

        for (OrderItem item : saved.getItems()) {
            saleService.createSale(item.getSmartphone().getSmartid(), item.getQuantity(),
                    customer, employee, saved.getOrderid());
        }

        cart.getItems().clear();
        cartRepository.save(cart);
        Order result = orderRepository.save(saved);
        realtimeService.orderChanged(result.getOrderid(), result.getOrderNumber(),
                result.getStatus());
        return result;
    }

    @Transactional(readOnly = true)
    public Order findById(Integer orderid) {
        return orderRepository.findWithDetailsById(orderid)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderid));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomer(Integer custid) {
        return orderRepository.findByCustomer_CustidOrderByOrderDateDesc(custid);
    }

    @Transactional
    public Order updateStatus(Integer orderid, String status) {
        Order order = findById(orderid);
        String target = status == null ? "" : status.toUpperCase(Locale.ROOT);
        if ("CANCELLED".equals(target)) {
            return cancel(orderid);
        }
        validateTransition(order.getStatus(), target);
        order.setStatus(target);
        if ("SHIPPED".equals(target)) {
            order.setShippedDate(LocalDateTime.now());
        } else if ("DELIVERED".equals(target)) {
            order.setDeliveredDate(LocalDateTime.now());
        }
        Order saved = orderRepository.save(order);
        realtimeService.orderChanged(saved.getOrderid(), saved.getOrderNumber(), saved.getStatus());
        return saved;
    }

    @Transactional
    public Order cancel(Integer orderid) {
        Order order = findById(orderid);
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            return order;
        }
        if ("DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }
        Employee employee = order.getEmployee();
        for (OrderItem item : order.getItems()) {
            inventoryService.recordRestore(item.getSmartphone(), item.getQuantity(),
                    order.getOrderid(), employee);
        }
        saleService.cancelForOrder(order.getOrderid());
        order.setStatus("CANCELLED");
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            order.setPaymentStatus("REFUNDED");
        }
        Order saved = orderRepository.save(order);
        realtimeService.orderChanged(saved.getOrderid(), saved.getOrderNumber(), saved.getStatus());
        return saved;
    }

    @Transactional(readOnly = true)
    public OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .orderitemid(item.getOrderitemid())
                        .smartid(item.getSmartphone() == null ? null : item.getSmartphone().getSmartid())
                        .smartname(item.getSmartphone() == null ? null : item.getSmartphone().getSmartname())
                        .smartimg(item.getSmartphone() == null ? null : item.getSmartphone().getSmartimg())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());
        return OrderDTO.builder()
                .orderid(order.getOrderid())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer() == null ? null : order.getCustomer().getCustname())
                .employeeName(order.getEmployee() == null ? null : order.getEmployee().getEmpname())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .couponCode(order.getCouponCode())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .orderDate(order.getOrderDate())
                .shippedDate(order.getShippedDate())
                .deliveredDate(order.getDeliveredDate())
                .notes(order.getNotes())
                .items(items)
                .build();
    }

    private Customer resolveCustomer(String username, CheckoutForm form) {
        if (form != null && form.getCustomerId() != null) {
            return customerRepository.findById(form.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id",
                            form.getCustomerId()));
        }
        return customerRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Select a customer before checking out"));
    }

    private void validateTransition(String current, String target) {
        String from = current == null ? "PENDING" : current.toUpperCase(Locale.ROOT);
        boolean valid = switch (from) {
            case "PENDING" -> "PROCESSING".equals(target) || "SHIPPED".equals(target);
            case "PROCESSING" -> "SHIPPED".equals(target);
            case "SHIPPED" -> "DELIVERED".equals(target);
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Invalid status transition from " + from + " to " + target);
        }
    }
}
