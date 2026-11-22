package com.smartshop.repository;

import com.smartshop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Customer> findAllByOrderByCustidAsc();

    @Query("""
            select c from Customer c
            where lower(c.custname) like lower(concat('%', :query, '%'))
               or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
               or coalesce(c.custpnum, '') like concat('%', :query, '%')
            order by c.custid asc
            """)
    List<Customer> search(@Param("query") String query);
}
