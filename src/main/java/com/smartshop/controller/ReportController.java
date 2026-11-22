package com.smartshop.controller;

import com.smartshop.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Downloads for the admin reports page. The sales export can be narrowed to a
 * date range; every other report covers all records.
 */
@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final MediaType CSV = MediaType.parseMediaType("text/csv");

    private final ReportService reportService;

    @GetMapping(value = "/sales.csv", produces = "text/csv")
    public ResponseEntity<byte[]> salesCsv(@RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return download("sales", "csv", CSV, reportService.toCsv(reportService.sales(from, to)));
    }

    @GetMapping("/sales.xlsx")
    public ResponseEntity<byte[]> salesXlsx(@RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return download("sales", "xlsx", XLSX,
                reportService.toXlsx(reportService.sales(from, to)));
    }

    @GetMapping("/sales.pdf")
    public ResponseEntity<byte[]> salesPdf(@RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return download("sales", "pdf", MediaType.APPLICATION_PDF,
                reportService.toPdf(reportService.sales(from, to)));
    }

    @GetMapping("/inventory.xlsx")
    public ResponseEntity<byte[]> inventoryXlsx() {
        return download("inventory", "xlsx", XLSX,
                reportService.toXlsx(reportService.inventory()));
    }

    @GetMapping("/inventory.pdf")
    public ResponseEntity<byte[]> inventoryPdf() {
        return download("inventory", "pdf", MediaType.APPLICATION_PDF,
                reportService.toPdf(reportService.inventory()));
    }

    @GetMapping("/stock-movements.csv")
    public ResponseEntity<byte[]> stockMovementsCsv() {
        return download("stock-movements", "csv", CSV,
                reportService.toCsv(reportService.inventoryHistory()));
    }

    @GetMapping("/customers.xlsx")
    public ResponseEntity<byte[]> customersXlsx() {
        return download("customers", "xlsx", XLSX,
                reportService.toXlsx(reportService.customers()));
    }

    @GetMapping("/employees.xlsx")
    public ResponseEntity<byte[]> employeesXlsx() {
        return download("employees", "xlsx", XLSX,
                reportService.toXlsx(reportService.employees()));
    }

    private ResponseEntity<byte[]> download(String name, String extension, MediaType type,
                                            byte[] body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + name + "." + extension)
                .contentType(type)
                .body(body);
    }
}