package com.smartshop.service;

import com.smartshop.dto.CartDTO;
import com.smartshop.dto.CartItemDTO;
import com.smartshop.entity.Cart;
import com.smartshop.entity.CartItem;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.CartRepository;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.SmartphoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final RealtimeService realtimeService;

    @Transactional
    public Cart getOrCreateCart(String username) {
        return cartRepository.findBySessionid(username).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setSessionid(username);
            customerRepository.findByEmailIgnoreCase(username).ifPresent(cart::setCustomer);
            return cartRepository.save(cart);
        });
    }

    @Transactional(readOnly = true)
    public Cart findCart(String username) {
        return cartRepository.findBySessionid(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "session", username));
    }

    @Transactional
    public Cart addItem(String username, String smartid, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Cart cart = getOrCreateCart(username);
        Smartphone smartphone = smartphoneRepository.findById(smartid)
                .orElseThrow(() -> new ResourceNotFoundException("Smartphone", "id", smartid));

        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getSmartphone() != null
                        && smartid.equals(item.getSmartphone().getSmartid()))
                .findFirst()
                .orElse(null);
        int requested = quantity + (existing == null ? 0 : existing.getQuantity());
        int available = smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty();
        if (requested > available) {
            throw new InsufficientStockException(smartphone.getSmartname(), requested, available);
        }

        if (existing == null) {
            cart.addItem(new CartItem(cart, smartphone, quantity, smartphone.getSmartprice()));
        } else {
            existing.setQuantity(requested);
            existing.setPriceAtAdd(smartphone.getSmartprice());
        }
        return announce(cartRepository.save(cart));
    }

    @Transactional
    public Cart updateQuantity(String username, Integer cartItemId, int quantity) {
        Cart cart = getOrCreateCart(username);
        CartItem item = cart.getItems().stream()
                .filter(candidate -> cartItemId.equals(candidate.getCartitemid()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));
        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            int available = item.getSmartphone().getSmartqty() == null ? 0 : item.getSmartphone().getSmartqty();
            if (quantity > available) {
                throw new InsufficientStockException(item.getSmartphone().getSmartname(), quantity, available);
            }
            item.setQuantity(quantity);
        }
        return announce(cartRepository.save(cart));
    }

    @Transactional
    public Cart removeItem(String username, Integer cartItemId) {
        Cart cart = getOrCreateCart(username);
        CartItem item = cart.getItems().stream()
                .filter(candidate -> cartItemId.equals(candidate.getCartitemid()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));
        cart.removeItem(item);
        return announce(cartRepository.save(cart));
    }

    @Transactional
    public Cart removeByProduct(String username, String smartid) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().stream()
                .filter(item -> item.getSmartphone() != null
                        && smartid.equals(item.getSmartphone().getSmartid()))
                .findFirst()
                .ifPresent(cart::removeItem);
        return announce(cartRepository.save(cart));
    }

    @Transactional
    public void clear(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartRepository.save(cart);
        announce(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getCartDTO(String username) {
        return cartRepository.findBySessionid(username)
                .map(this::toDTO)
                .orElseGet(() -> new CartDTO(null, List.of(), BigDecimal.ZERO));
    }

    @Transactional(readOnly = true)
    public int itemCount(String username) {
        return cartRepository.findBySessionid(username)
                .map(cart -> cart.getItems().stream()
                        .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                        .sum())
                .orElse(0);
    }

    /**
     * Publishes the new item count to the signed-in user's private broker queue.
     */
    private Cart announce(Cart cart) {
        int count = cart.getItems().stream()
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();
        realtimeService.cartChanged(cart.getSessionid(), count);
        return cart;
    }

    public CartDTO toDTO(Cart cart) {
        List<CartItemDTO> items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            BigDecimal price = item.getPriceAtAdd() == null ? BigDecimal.ZERO : item.getPriceAtAdd();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            Smartphone smartphone = item.getSmartphone();
            items.add(new CartItemDTO(item.getCartitemid(),
                    smartphone == null ? null : smartphone.getSmartid(),
                    smartphone == null ? null : smartphone.getSmartname(),
                    smartphone == null ? null : smartphone.getSmartimg(),
                    item.getQuantity(), price, subtotal));
        }
        return new CartDTO(cart.getCartid(), items);
    }
}
