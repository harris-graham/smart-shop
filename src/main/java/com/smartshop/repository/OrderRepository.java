package com.smartshop.repository;

import com.smartshop.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"items", "items.smartphone", "customer", "employee"})
    @Query("select distinct o from Order o where o.orderid = :orderid")
    Optional<Order> findWithDetailsById(@Param("orderid") Integer orderid);

    @EntityGraph(attributePaths = {"items", "items.smartphone", "customer", "employee"})
    List<Order> findByCustomer_CustidOrderByOrderDateDesc(Integer custid);

    @EntityGraph(attributePaths = {"items", "items.smartphone", "customer", "employee"})
    List<Order> findAllByOrderByOrderDateDesc();

    @EntityGraph(attributePaths = {"items", "items.smartphone", "customer", "employee"})
    List<Order> findByStatusOrderByOrderDateDesc(String status);

    long countByStatus(String status);

    long countByPaymentStatus(String status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status <> 'CANCELLED'")
    BigDecimal sumRevenue();
}
