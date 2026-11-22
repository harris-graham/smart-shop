package com.smartshop.controller.api;

import com.smartshop.dto.AdjustStockRequest;
import com.smartshop.entity.Employee;
import com.smartshop.entity.InventoryHistory;
import com.smartshop.entity.Smartphone;
import com.smartshop.service.InventoryService;
import com.smartshop.service.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryRestController {

    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;

    @GetMapping("/low-stock")
    public List<Smartphone> lowStock(@RequestParam(defaultValue = "3") int threshold) {
        return inventoryService.lowStock(threshold);
    }

    @GetMapping("/history")
    public List<InventoryHistory> history() {
        return inventoryService.findAll();
    }

    @PostMapping("/{smartid}/restock")
    public Smartphone restock(@PathVariable String smartid, @RequestParam int quantity) {
        Employee employee = securityUtils.currentEmployee().orElse(null);
        return inventoryService.restock(smartid, quantity, employee);
    }

    @PostMapping("/{smartid}/adjust")
    public Smartphone adjust(@PathVariable String smartid,
                             @Valid @RequestBody AdjustStockRequest request) {
        Employee employee = securityUtils.currentEmployee().orElse(null);
        return inventoryService.adjustStock(smartid, request.getNewQuantity(), employee,
                request.getNotes());
    }
}
