package com.smartshop.repository;

import com.smartshop.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    @EntityGraph(attributePaths = {"smartphone"})
    List<CartItem> findByCart_Cartid(Integer cartid);

    Optional<CartItem> findByCart_CartidAndSmartphone_Smartid(Integer cartid, String smartid);

    void deleteByCart_Cartid(Integer cartid);
}
