package com.smartshop.service;

import com.smartshop.entity.Employee;
import com.smartshop.entity.InventoryHistory;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.InventoryHistoryRepository;
import com.smartshop.repository.SmartphoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryHistoryRepository historyRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final RealtimeService realtimeService;

    @Transactional(readOnly = true)
    public List<InventoryHistory> findAll() {
        return historyRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> findByProduct(String smartid) {
        return historyRepository.findBySmartphone_SmartidOrderByCreatedAtDesc(smartid);
    }

    @Transactional(readOnly = true)
    public List<Smartphone> lowStock(int threshold) {
        return smartphoneRepository.findBySmartqtyLessThanEqualOrderBySmartqtyAsc(threshold);
    }

    @Transactional
    public Smartphone restock(String smartid, int quantity, Employee changedBy) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }
        Smartphone smartphone = smartphoneRepository.findById(smartid)
                .orElseThrow(() -> new ResourceNotFoundException("Smartphone", "id", smartid));
        int previous = smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty();
        int updated = previous + quantity;
        smartphone.setSmartqty(updated);
        smartphoneRepository.save(smartphone);
        record(smartphone, previous, updated, "RESTOCK", null,
                "Restocked " + quantity + " unit(s)", changedBy);
        announce(smartphone, updated);
        return smartphone;
    }

    @Transactional
    public Smartphone adjustStock(String smartid, int newQuantity, Employee changedBy, String notes) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        Smartphone smartphone = smartphoneRepository.findById(smartid)
                .orElseThrow(() -> new ResourceNotFoundException("Smartphone", "id", smartid));
        int previous = smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty();
        smartphone.setSmartqty(newQuantity);
        smartphoneRepository.save(smartphone);
        record(smartphone, previous, newQuantity, "ADJUSTMENT", null, notes, changedBy);
        announce(smartphone, newQuantity);
        return smartphone;
    }

    @Transactional
    public void recordSale(Smartphone smartphone, int quantity, Integer referenceId, Employee changedBy) {
        int previous = smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty();
        int updated = previous - quantity;
        if (updated < 0) {
            throw new InsufficientStockException(smartphone.getSmartname(), quantity, previous);
        }
        smartphone.setSmartqty(updated);
        smartphoneRepository.save(smartphone);
        record(smartphone, previous, updated, "SALE", referenceId,
                "Sold " + quantity + " unit(s)", changedBy);
        announce(smartphone, updated);
    }

    @Transactional
    public void recordRestore(Smartphone smartphone, int quantity, Integer referenceId, Employee changedBy) {
        int previous = smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty();
        int updated = previous + quantity;
        smartphone.setSmartqty(updated);
        smartphoneRepository.save(smartphone);
        record(smartphone, previous, updated, "RESTOCK", referenceId,
                "Restored " + quantity + " unit(s) after cancellation", changedBy);
        announce(smartphone, updated);
    }

    private void announce(Smartphone smartphone, int quantity) {
        realtimeService.stockChanged(smartphone.getSmartid(), smartphone.getSmartname(), quantity);
    }

    private void record(Smartphone smartphone, int previous, int updated, String changeType,
                        Integer referenceId, String notes, Employee changedBy) {
        historyRepository.save(new InventoryHistory(smartphone, updated - previous, previous,
                updated, changeType, referenceId, notes, changedBy));
    }
}
