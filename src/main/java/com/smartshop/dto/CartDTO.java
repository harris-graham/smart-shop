package com.smartshop.dto;

import com.smartshop.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Integer cartid;
    private List<CartItemDTO> items;
    private BigDecimal total;

    public CartDTO(Integer cartid, List<CartItemDTO> items) {
        this.cartid = cartid;
        this.items = items;
        this.total = items.stream()
                .map(item -> item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.stream().mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum();
    }
}
