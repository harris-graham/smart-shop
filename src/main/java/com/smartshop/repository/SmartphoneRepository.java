package com.smartshop.repository;

import com.smartshop.entity.Smartphone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmartphoneRepository extends JpaRepository<Smartphone, String>,
        JpaSpecificationExecutor<Smartphone> {

    List<Smartphone> findBySmartqtyLessThanEqualOrderBySmartqtyAsc(Integer threshold);

    List<Smartphone> findBySmartqty(Integer smartqty);

    List<Smartphone> findBySmartqtyGreaterThanAndSmartqtyLessThanEqualOrderBySmartqtyAsc(
            Integer min, Integer max);

    long countBySmartqtyLessThanEqual(Integer threshold);

    boolean existsBySmartnameIgnoreCase(String smartname);
}
