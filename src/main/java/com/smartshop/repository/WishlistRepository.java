package com.smartshop.repository;

import com.smartshop.entity.Wishlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    @EntityGraph(attributePaths = {"smartphone"})
    List<Wishlist> findByCustomer_CustidOrderByCreatedAtDesc(Integer custid);

    boolean existsByCustomer_CustidAndSmartphone_Smartid(Integer custid, String smartid);

    Optional<Wishlist> findByCustomer_CustidAndSmartphone_Smartid(Integer custid, String smartid);

    void deleteByCustomer_CustidAndSmartphone_Smartid(Integer custid, String smartid);
}
