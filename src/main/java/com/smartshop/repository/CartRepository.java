package com.smartshop.repository;

import com.smartshop.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    @EntityGraph(attributePaths = {"items", "items.smartphone", "customer"})
    Optional<Cart> findBySessionid(String sessionid);

    @EntityGraph(attributePaths = {"items", "items.smartphone", "customer"})
    Optional<Cart> findByCustomer_Custid(Integer custid);

    @EntityGraph(attributePaths = {"items", "items.smartphone", "customer"})
    @Query("select c from Cart c where c.cartid = :cartid")
    Optional<Cart> findWithItemsById(@Param("cartid") Integer cartid);
}
