package com.smartshop.service;

import com.smartshop.entity.Cart;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.repository.CartRepository;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.SmartphoneRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SmartphoneRepository smartphoneRepository;

    @Mock
    private RealtimeService realtimeService;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItemCreatesCartLine() {
        Cart cart = new Cart();
        cart.setCartid(1);
        cart.setSessionid("customer@example.com");
        Smartphone smartphone = new Smartphone("SP-1", "Phone", new BigDecimal("100.00"),
                5, 8, 128, null);

        when(cartRepository.findBySessionid("customer@example.com")).thenReturn(Optional.of(cart));
        when(smartphoneRepository.findById("SP-1")).thenReturn(Optional.of(smartphone));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addItem("customer@example.com", "SP-1", 2);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.getItems().get(0).getPriceAtAdd()).isEqualByComparingTo("100.00");
        verify(realtimeService).cartChanged("customer@example.com", 2);
    }

    @Test
    void addItemRejectsQuantityAboveStock() {
        Cart cart = new Cart();
        cart.setSessionid("customer@example.com");
        Smartphone smartphone = new Smartphone("SP-1", "Phone", new BigDecimal("100.00"),
                1, 8, 128, null);

        when(cartRepository.findBySessionid("customer@example.com")).thenReturn(Optional.of(cart));
        when(smartphoneRepository.findById("SP-1")).thenReturn(Optional.of(smartphone));

        assertThatThrownBy(() -> cartService.addItem("customer@example.com", "SP-1", 2))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Phone");
    }
}
