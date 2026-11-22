package com.smartshop.repository;

import com.smartshop.entity.Sale;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {

    @EntityGraph(attributePaths = {"smartphone", "customer", "employee"})
    List<Sale> findAllByOrderBySaleDateDesc();

    @EntityGraph(attributePaths = {"smartphone", "customer", "employee"})
    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"smartphone", "customer", "employee"})
    List<Sale> findTop20ByOrderBySaleDateDesc();

    @Query("select coalesce(sum(s.saletotal), 0) from Sale s where s.status <> 'CANCELLED'")
    BigDecimal sumRevenue();

    @Query("select coalesce(sum(s.saletotal), 0) from Sale s where s.saleDate between :start and :end and s.status <> 'CANCELLED'")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    List<Sale> findByOrderid(Integer orderid);

    @Modifying
    @Query("update Sale s set s.status = 'CANCELLED' where s.orderid = :orderid")
    int cancelByOrderid(@Param("orderid") Integer orderid);

    @Query("""
            select s.smartphone.smartid, s.smartphone.smartname,
                   coalesce(sum(s.saleqty), 0), coalesce(sum(s.saletotal), 0)
            from Sale s
            where s.status <> 'CANCELLED'
            group by s.smartphone.smartid, s.smartphone.smartname
            order by coalesce(sum(s.saleqty), 0) desc
            """)
    List<Object[]> findTopSelling(Pageable pageable);

    @Query("""
            select s.employee.empid, s.employee.empname,
                   coalesce(sum(s.saleqty), 0), coalesce(sum(s.saletotal), 0)
            from Sale s
            where s.employee is not null and s.status <> 'CANCELLED'
            group by s.employee.empid, s.employee.empname
            order by coalesce(sum(s.saletotal), 0) desc
            """)
    List<Object[]> findEmployeePerformance();

    @Query("""
            select s.customer.custid, s.customer.custname,
                   count(s), coalesce(sum(s.saletotal), 0)
            from Sale s
            where s.customer is not null and s.status <> 'CANCELLED'
            group by s.customer.custid, s.customer.custname
            order by coalesce(sum(s.saletotal), 0) desc
            """)
    List<Object[]> findCustomerPerformance(Pageable pageable);
}
