package com.smartshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartItemID")
    private Integer cartitemid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CartID")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SmartID")
    private Smartphone smartphone;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "PriceAtAdd", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAdd;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public CartItem(Cart cart, Smartphone smartphone, Integer quantity, BigDecimal priceAtAdd) {
        this.cart = cart;
        this.smartphone = smartphone;
        this.quantity = quantity;
        this.priceAtAdd = priceAtAdd;
    }
}
