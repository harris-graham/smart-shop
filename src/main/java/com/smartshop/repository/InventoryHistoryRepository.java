package com.smartshop.repository;

import com.smartshop.entity.InventoryHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Integer> {

    @EntityGraph(attributePaths = {"smartphone", "changedBy"})
    List<InventoryHistory> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"smartphone", "changedBy"})
    List<InventoryHistory> findBySmartphone_SmartidOrderByCreatedAtDesc(String smartid);
}
