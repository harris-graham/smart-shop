package com.smartshop.controller.api;

import com.smartshop.dto.PageResponse;
import com.smartshop.dto.SmartphoneForm;
import com.smartshop.entity.Employee;
import com.smartshop.entity.Smartphone;
import com.smartshop.service.SecurityUtils;
import com.smartshop.service.SmartphoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/smartphones")
@RequiredArgsConstructor
public class SmartphoneRestController {

    private final SmartphoneService smartphoneService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public PageResponse<Smartphone> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) BigDecimal minPrice,
                                         @RequestParam(required = false) BigDecimal maxPrice,
                                         @RequestParam(required = false) Integer minRam,
                                         @RequestParam(required = false) Integer minStorage,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Page<Smartphone> result = smartphoneService.search(keyword, minPrice, maxPrice,
                minRam, minStorage, PageRequest.of(page, size, Sort.by("smartname").ascending()));
        return PageResponse.from(result);
    }

    @GetMapping("/{smartid}")
    public Smartphone get(@PathVariable String smartid) {
        return smartphoneService.findById(smartid);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Smartphone create(@Valid @RequestBody SmartphoneForm form) {
        return smartphoneService.create(form, null);
    }

    @PutMapping("/{smartid}")
    @PreAuthorize("hasRole('ADMIN')")
    public Smartphone update(@PathVariable String smartid,
                             @Valid @RequestBody SmartphoneForm form) {
        return smartphoneService.update(smartid, form, null);
    }

    @DeleteMapping("/{smartid}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String smartid) {
        smartphoneService.delete(smartid);
    }

    @PostMapping("/{smartid}/restock")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Smartphone restock(@PathVariable String smartid, @RequestParam int quantity) {
        Employee employee = securityUtils.currentEmployee().orElse(null);
        return smartphoneService.addStock(smartid, quantity, employee);
    }
}
