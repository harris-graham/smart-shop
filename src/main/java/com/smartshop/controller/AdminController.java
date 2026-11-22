package com.smartshop.controller;

import com.smartshop.dto.AdjustStockRequest;
import com.smartshop.dto.CustomerForm;
import com.smartshop.dto.EmployeeForm;
import com.smartshop.dto.SmartphoneForm;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.entity.InventoryHistory;
import com.smartshop.entity.Order;
import com.smartshop.entity.Review;
import com.smartshop.entity.Sale;
import com.smartshop.entity.Smartphone;
import com.smartshop.service.CustomerService;
import com.smartshop.service.DashboardService;
import com.smartshop.service.EmployeeService;
import com.smartshop.service.InventoryService;
import com.smartshop.service.OrderService;
import com.smartshop.service.ReviewService;
import com.smartshop.service.SaleService;
import com.smartshop.service.SecurityUtils;
import com.smartshop.service.SmartphoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SmartphoneService smartphoneService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final SaleService saleService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final InventoryService inventoryService;
    private final DashboardService dashboardService;
    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @Value("${smartshop.low-stock-threshold:3}")
    private int lowStockThreshold;

    @GetMapping({"", "/"})
    public String index() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        model.addAttribute("lowStockThreshold", lowStockThreshold);
        model.addAttribute("lowStock", smartphoneService.lowStock(lowStockThreshold));
        return "admin/dashboard";
    }

    @GetMapping("/smartphones")
    public String smartphones(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {
        Page<Smartphone> products = smartphoneService.search(keyword, null, null, null, null,
                PageRequest.of(page, size, Sort.by("smartname").ascending()));
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("smartphoneForm", new SmartphoneForm());
        model.addAttribute("lowStock", smartphoneService.lowStock(lowStockThreshold));
        model.addAttribute("lowStockThreshold", lowStockThreshold);
        return "admin/smartphones";
    }

    @PostMapping("/smartphones")
    public String createSmartphone(@Valid @ModelAttribute("smartphoneForm") SmartphoneForm form,
                                   BindingResult bindingResult,
                                   @RequestParam(value = "image", required = false) MultipartFile image,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the product fields");
            return "redirect:/admin/smartphones";
        }
        smartphoneService.create(form, image);
        redirectAttributes.addFlashAttribute("message", "Product added");
        return "redirect:/admin/smartphones";
    }

    @PostMapping("/smartphones/{smartid}")
    public String updateSmartphone(@PathVariable String smartid,
                                   @Valid @ModelAttribute("smartphoneForm") SmartphoneForm form,
                                   BindingResult bindingResult,
                                   @RequestParam(value = "image", required = false) MultipartFile image,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the product fields");
            return "redirect:/admin/smartphones";
        }
        smartphoneService.update(smartid, form, image);
        redirectAttributes.addFlashAttribute("message", "Product updated");
        return "redirect:/admin/smartphones";
    }

    @PostMapping("/smartphones/{smartid}/delete")
    public String deleteSmartphone(@PathVariable String smartid,
                                   RedirectAttributes redirectAttributes) {
        try {
            smartphoneService.delete(smartid);
            redirectAttributes.addFlashAttribute("message", "Product deleted");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/smartphones";
    }

    @PostMapping("/smartphones/{smartid}/restock")
    public String restockSmartphone(@PathVariable String smartid,
                                    @RequestParam int quantity,
                                    RedirectAttributes redirectAttributes) {
        Employee employee = securityUtils.currentEmployee().orElse(null);
        try {
            smartphoneService.addStock(smartid, quantity, employee);
            redirectAttributes.addFlashAttribute("message", "Stock updated");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/employees")
    public String employees(@RequestParam(required = false) String keyword, Model model) {
        List<Employee> employees = employeeService.search(keyword);
        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword);
        model.addAttribute("employeeForm", new EmployeeForm());
        return "admin/employees";
    }

    @PostMapping("/employees")
    public String createEmployee(@Valid @ModelAttribute("employeeForm") EmployeeForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the employee fields");
            return "redirect:/admin/employees";
        }
        try {
            employeeService.create(form);
            redirectAttributes.addFlashAttribute("message", "Employee added");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{empid}")
    public String updateEmployee(@PathVariable Integer empid,
                                 @Valid @ModelAttribute("employeeForm") EmployeeForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the employee fields");
            return "redirect:/admin/employees";
        }
        try {
            employeeService.update(empid, form);
            redirectAttributes.addFlashAttribute("message", "Employee updated");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{empid}/delete")
    public String deleteEmployee(@PathVariable Integer empid,
                                 RedirectAttributes redirectAttributes) {
        try {
            employeeService.delete(empid);
            redirectAttributes.addFlashAttribute("message", "Employee removed");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String keyword, Model model) {
        List<Customer> customers = customerService.search(keyword);
        model.addAttribute("customers", customers);
        model.addAttribute("keyword", keyword);
        model.addAttribute("customerForm", new CustomerForm());
        return "admin/customers";
    }

    @PostMapping("/customers")
    public String createCustomer(@Valid @ModelAttribute("customerForm") CustomerForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the customer fields");
            return "redirect:/admin/customers";
        }
        try {
            customerService.create(form);
            redirectAttributes.addFlashAttribute("message", "Customer added");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/{custid}")
    public String updateCustomer(@PathVariable Integer custid,
                                 @Valid @ModelAttribute("customerForm") CustomerForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please check the customer fields");
            return "redirect:/admin/customers";
        }
        try {
            customerService.update(custid, form);
            redirectAttributes.addFlashAttribute("message", "Customer updated");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/{custid}/delete")
    public String deleteCustomer(@PathVariable Integer custid,
                                 RedirectAttributes redirectAttributes) {
        try {
            customerService.delete(custid);
            redirectAttributes.addFlashAttribute("message", "Customer deleted");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/sales")
    public String sales(@RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                        Model model) {
        List<Sale> sales = (from != null && to != null)
                ? saleService.findBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX))
                : saleService.findAll();
        model.addAttribute("sales", sales);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/sales";
    }

    @GetMapping("/sales/export")
    public ResponseEntity<byte[]> exportSales(@RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] body = reportService.toCsv(reportService.sales(from, to));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders = (status == null || status.isBlank())
                ? orderService.findAll()
                : orderService.findAll().stream()
                        .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                        .toList();
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        return "admin/orders";
    }

    @PostMapping("/orders/{orderid}/status")
    public String updateOrderStatus(@PathVariable Integer orderid,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(orderid, status);
            redirectAttributes.addFlashAttribute("message", "Order status updated");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/reviews")
    public String reviews(@RequestParam(required = false) String status, Model model) {
        List<Review> reviews = switch (status == null ? "" : status.toUpperCase(Locale.ROOT)) {
            case "PENDING" -> reviewService.pending();
            default -> reviewService.findAll();
        };
        model.addAttribute("reviews", reviews);
        model.addAttribute("status", status);
        model.addAttribute("pendingCount", reviewService.pendingCount());
        return "admin/reviews";
    }

    @PostMapping("/reviews/{reviewid}/moderate")
    public String moderateReview(@PathVariable Integer reviewid,
                                 @RequestParam String status,
                                 RedirectAttributes redirectAttributes) {
        try {
            reviewService.moderate(reviewid, status);
            redirectAttributes.addFlashAttribute("message", "Review updated");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/smartphones/bulk-price")
    public String bulkPrice(@RequestParam String mode,
                            @RequestParam BigDecimal value,
                            @RequestParam(defaultValue = "ALL") String scope,
                            RedirectAttributes redirectAttributes) {
        try {
            int changed = smartphoneService.bulkPriceChange(mode, value, scope, lowStockThreshold);
            redirectAttributes.addFlashAttribute("message", changed + " product(s) repriced");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/smartphones/export.csv")
    public ResponseEntity<byte[]> exportCatalogue() {
        byte[] body = smartphoneService.exportCsv().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @PostMapping("/smartphones/import")
    public String importCatalogue(@RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirectAttributes) {
        try {
            SmartphoneService.ImportResult result = smartphoneService.importCsv(file);
            String summary = result.created() + " created, " + result.updated() + " updated";
            if (!result.errors().isEmpty()) {
                summary += "; " + result.errors().size() + " line(s) skipped: "
                        + String.join(" | ", result.errors());
            }
            redirectAttributes.addFlashAttribute("message", summary);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        List<InventoryHistory> history = inventoryService.findAll();
        model.addAttribute("history", history);
        model.addAttribute("lowStock", inventoryService.lowStock(lowStockThreshold));
        model.addAttribute("lowStockThreshold", lowStockThreshold);
        model.addAttribute("adjustStockRequest", new AdjustStockRequest());
        return "admin/inventory";
    }

    @PostMapping("/inventory/{smartid}/adjust")
    public String adjustStock(@PathVariable String smartid,
                              @Valid @ModelAttribute AdjustStockRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Enter a valid stock level");
            return "redirect:/admin/inventory";
        }
        Employee employee = securityUtils.currentEmployee().orElse(null);
        try {
            inventoryService.adjustStock(smartid, request.getNewQuantity(), employee,
                    request.getNotes());
            redirectAttributes.addFlashAttribute("message", "Stock adjusted");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        return "admin/reports";
    }
}
