package com.smartshop.service;

import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.entity.Sale;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.SaleRepository;
import com.smartshop.repository.SmartphoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<Sale> findAll() {
        return saleRepository.findAllByOrderBySaleDateDesc();
    }

    @Transactional(readOnly = true)
    public List<Sale> findBetween(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
    }

    @Transactional
    public Sale createSale(String smartid, int quantity, Customer customer, Employee employee,
                           Integer referenceId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Smartphone smartphone = smartphoneRepository.findById(smartid)
                .orElseThrow(() -> new ResourceNotFoundException("Smartphone", "id", smartid));
        int available = smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty();
        if (available < quantity) {
            throw new InsufficientStockException(smartphone.getSmartname(), quantity, available);
        }
        BigDecimal total = smartphone.getSmartprice().multiply(BigDecimal.valueOf(quantity));
        Sale sale = new Sale(quantity, total, smartphone, customer, employee);
        sale.setStatus("COMPLETED");
        sale.setOrderid(referenceId);
        Sale saved = saleRepository.save(sale);
        inventoryService.recordSale(smartphone, quantity,
                referenceId == null ? saved.getSaleid() : referenceId, employee);
        return saved;
    }

    @Transactional
    public int cancelForOrder(Integer orderid) {
        if (orderid == null) {
            return 0;
        }
        return saleRepository.cancelByOrderid(orderid);
    }
}
