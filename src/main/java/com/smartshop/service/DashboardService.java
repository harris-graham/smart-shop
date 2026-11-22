package com.smartshop.service;

import com.smartshop.dto.CustomerPerformance;
import com.smartshop.dto.DailySales;
import com.smartshop.dto.DashboardStats;
import com.smartshop.dto.EmployeePerformance;
import com.smartshop.dto.TopProduct;
import com.smartshop.entity.Sale;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.ReviewRepository;
import com.smartshop.repository.SaleRepository;
import com.smartshop.repository.SmartphoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SmartphoneRepository smartphoneRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final SaleRepository saleRepository;

    @Value("${smartshop.low-stock-threshold:3}")
    private int lowStockThreshold;

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfWeek = today.minusDays(6).atStartOfDay();

        List<Sale> recentSales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(
                startOfWeek, endOfToday);
        Map<LocalDate, List<Sale>> salesByDay = new LinkedHashMap<>();
        for (int offset = 6; offset >= 0; offset--) {
            salesByDay.put(today.minusDays(offset), new ArrayList<>());
        }
        for (Sale sale : recentSales) {
            if (sale.getSaleDate() != null) {
                salesByDay.computeIfAbsent(sale.getSaleDate().toLocalDate(), key -> new ArrayList<>())
                        .add(sale);
            }
        }
        List<DailySales> lastSevenDays = salesByDay.entrySet().stream()
                .map(entry -> new DailySales(entry.getKey(), entry.getValue().size(),
                        entry.getValue().stream()
                                .map(Sale::getSaletotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();

        return DashboardStats.builder()
                .productCount(smartphoneRepository.count())
                .customerCount(customerRepository.count())
                .employeeCount(employeeRepository.count())
                .orderCount(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus("PENDING"))
                .pendingReviews(reviewRepository.countByStatus("PENDING"))
                .lowStockCount(smartphoneRepository.countBySmartqtyLessThanEqual(lowStockThreshold))
                .totalRevenue(saleRepository.sumRevenue())
                .revenueToday(saleRepository.sumRevenueBetween(startOfToday, endOfToday))
                .revenueThisMonth(saleRepository.sumRevenueBetween(startOfMonth, endOfToday))
                .topProducts(topProducts())
                .employeePerformance(employeePerformance())
                .topCustomers(topCustomers())
                .lastSevenDays(lastSevenDays)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TopProduct> topProducts() {
        List<TopProduct> products = new ArrayList<>();
        for (Object[] row : saleRepository.findTopSelling(PageRequest.of(0, 5))) {
            products.add(new TopProduct((String) row[0], (String) row[1],
                    ((Number) row[2]).longValue(), (BigDecimal) row[3]));
        }
        return products;
    }

    @Transactional(readOnly = true)
    public List<EmployeePerformance> employeePerformance() {
        List<EmployeePerformance> performance = new ArrayList<>();
        for (Object[] row : saleRepository.findEmployeePerformance()) {
            performance.add(new EmployeePerformance((Integer) row[0], (String) row[1],
                    ((Number) row[2]).longValue(), (BigDecimal) row[3]));
        }
        return performance;
    }

    @Transactional(readOnly = true)
    public List<CustomerPerformance> topCustomers() {
        List<CustomerPerformance> customers = new ArrayList<>();
        for (Object[] row : saleRepository.findCustomerPerformance(PageRequest.of(0, 5))) {
            customers.add(new CustomerPerformance((Integer) row[0], (String) row[1],
                    ((Number) row[2]).longValue(), (BigDecimal) row[3]));
        }
        return customers;
    }
}
