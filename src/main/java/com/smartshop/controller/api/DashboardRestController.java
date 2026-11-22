package com.smartshop.controller.api;

import com.smartshop.dto.DashboardStats;
import com.smartshop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardRestController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardStats dashboard() {
        return dashboardService.getStats();
    }
}
