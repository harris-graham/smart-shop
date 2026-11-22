package com.smartshop.repository;

import com.smartshop.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @EntityGraph(attributePaths = {"customer", "smartphone"})
    List<Review> findBySmartphone_SmartidAndStatusOrderByCreatedAtDesc(String smartid, String status);

    @EntityGraph(attributePaths = {"customer", "smartphone"})
    List<Review> findByStatusOrderByCreatedAtDesc(String status);

    @EntityGraph(attributePaths = {"customer", "smartphone"})
    List<Review> findAllByOrderByCreatedAtDesc();

    Optional<Review> findBySmartphone_SmartidAndCustomer_Custid(String smartid, Integer custid);

    long countBySmartphone_SmartidAndStatus(String smartid, String status);

    long countByStatus(String status);

    @Query("""
            select coalesce(avg(r.rating), 0)
            from Review r
            where r.smartphone.smartid = :smartid and r.status = 'APPROVED'
            """)
    Double averageRating(@Param("smartid") String smartid);
}
