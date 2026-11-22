package com.smartshop.service;

import com.smartshop.dto.CheckoutForm;
import com.smartshop.dto.PaymentResult;
import com.smartshop.entity.Cart;
import com.smartshop.entity.CartItem;
import com.smartshop.entity.Coupon;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Order;
import com.smartshop.entity.OrderItem;
import com.smartshop.entity.Smartphone;
import com.smartshop.repository.CartRepository;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import com.smartshop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SaleService saleService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private CouponService couponService;

    @Mock
    private RealtimeService realtimeService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createFromCartBuildsPaidOrderAndClearsCart() {
        Customer customer = new Customer();
        customer.setCustid(7);
        customer.setCustname("Alice");
        customer.setEmail("alice@example.com");

        Smartphone smartphone = new Smartphone("SP-1", "Phone", new BigDecimal("100.00"),
                5, 8, 128, null);
        Cart cart = new Cart();
        cart.setCartid(3);
        cart.setSessionid("alice@example.com");
        cart.addItem(new CartItem(cart, smartphone, 2, new BigDecimal("100.00")));

        when(cartRepository.findBySessionid("alice@example.com")).thenReturn(Optional.of(cart));
        when(customerRepository.findByEmailIgnoreCase("alice@example.com"))
                .thenReturn(Optional.of(customer));
        when(employeeRepository.findByEmpunameIgnoreCase("alice@example.com"))
                .thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentService.process(any(Order.class)))
                .thenReturn(new PaymentResult(true, "tx-1", "paid"));

        Order order = orderService.createFromCart("alice@example.com", new CheckoutForm());

        assertThat(order.getTotalAmount()).isEqualByComparingTo("200.00");
        assertThat(order.getPaymentStatus()).isEqualTo("PAID");
        assertThat(order.getPaymentId()).isEqualTo("tx-1");
        assertThat(order.getItems()).hasSize(1);
        assertThat(cart.getItems()).isEmpty();
        verify(saleService).createSale(eq("SP-1"), eq(2), eq(customer), eq(null), any());
        verify(cartRepository).save(cart);
    }

    @Test
    void appliesCouponDiscountAndCountsUsage() {
        Customer customer = new Customer();
        customer.setCustid(7);
        customer.setCustname("Alice");
        customer.setEmail("alice@example.com");

        Smartphone smartphone = new Smartphone("SP-1", "Phone", new BigDecimal("100.00"),
                5, 8, 128, null);
        Cart cart = new Cart();
        cart.setSessionid("alice@example.com");
        cart.addItem(new CartItem(cart, smartphone, 2, new BigDecimal("100.00")));

        Coupon coupon = new Coupon();
        coupon.setCouponid(5);
        coupon.setCode("WELCOME10");

        CheckoutForm form = new CheckoutForm();
        form.setCouponCode("WELCOME10");

        when(cartRepository.findBySessionid("alice@example.com")).thenReturn(Optional.of(cart));
        when(customerRepository.findByEmailIgnoreCase("alice@example.com"))
                .thenReturn(Optional.of(customer));
        when(employeeRepository.findByEmpunameIgnoreCase("alice@example.com"))
                .thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentService.process(any(Order.class)))
                .thenReturn(new PaymentResult(true, "tx-2", "paid"));
        when(couponService.validate(eq("WELCOME10"), any(BigDecimal.class)))
                .thenReturn(new CouponService.Discount(coupon, new BigDecimal("20.00")));

        Order order = orderService.createFromCart("alice@example.com", form);

        assertThat(order.getCouponCode()).isEqualTo("WELCOME10");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("20.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("180.00");
        verify(couponService).recordUsage(5);
        verify(realtimeService).orderChanged(any(), any(), eq("PENDING"));
    }

    @Test
    void cancelRestoresStock() {
        Smartphone smartphone = new Smartphone("SP-1", "Phone", new BigDecimal("100.00"),
                1, 8, 128, null);
        Order order = new Order();
        order.setOrderid(12);
        order.setStatus("PENDING");
        order.setPaymentStatus("PAID");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.addItem(new OrderItem(order, smartphone, 1, new BigDecimal("100.00"),
                new BigDecimal("100.00")));

        when(orderRepository.findWithDetailsById(12)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order cancelled = orderService.cancel(12);

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelled.getPaymentStatus()).isEqualTo("REFUNDED");
        verify(inventoryService).recordRestore(eq(smartphone), eq(1), eq(12), eq(null));
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Order order = new Order();
        order.setOrderid(1);
        order.setStatus("DELIVERED");

        when(orderRepository.findWithDetailsById(1)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1, "PROCESSING"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void requiresCustomerForEmployeeCheckout() {
        Smartphone smartphone = new Smartphone("SP-1", "Phone", new BigDecimal("100.00"),
                5, 8, 128, null);
        Cart cart = new Cart();
        cart.setSessionid("staff");
        cart.addItem(new CartItem(cart, smartphone, 1, new BigDecimal("100.00")));

        when(cartRepository.findBySessionid("staff")).thenReturn(Optional.of(cart));
        when(customerRepository.findByEmailIgnoreCase("staff")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createFromCart("staff", new CheckoutForm()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Select a customer");
    }
}
