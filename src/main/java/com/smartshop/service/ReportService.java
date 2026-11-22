package com.smartshop.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.entity.InventoryHistory;
import com.smartshop.entity.Sale;
import com.smartshop.entity.Smartphone;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import com.smartshop.repository.InventoryHistoryRepository;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.SaleRepository;
import com.smartshop.repository.SmartphoneRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds the downloadable admin reports. Every report is described once as a
 * {@link ReportTable} and then rendered as CSV, XLSX or PDF, so the three
 * formats can never drift apart.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SaleRepository saleRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final OrderRepository orderRepository;

    @Value("${smartshop.commission-rate:0.05}")
    private double commissionRate;

    @Transactional(readOnly = true)
    public ReportTable sales(LocalDate from, LocalDate to) {
        List<Sale> sales = findSales(from, to);
        List<String[]> rows = new ArrayList<>();
        BigDecimal revenue = BigDecimal.ZERO;
        int units = 0;
        for (Sale sale : sales) {
            BigDecimal total = sale.getSaletotal() == null ? BigDecimal.ZERO : sale.getSaletotal();
            revenue = revenue.add(total);
            units += sale.getSaleqty() == null ? 0 : sale.getSaleqty();
            rows.add(new String[]{
                    String.valueOf(sale.getSaleid()),
                    text(sale.getSaleDate()),
                    text(sale.getSmartphone() == null ? "" : sale.getSmartphone().getSmartname()),
                    String.valueOf(sale.getSaleqty()),
                    money(total),
                    text(sale.getCustomer() == null ? "" : sale.getCustomer().getCustname()),
                    text(sale.getEmployee() == null ? "" : sale.getEmployee().getEmpname()),
                    text(sale.getStatus())
            });
        }
        List<String> headers = List.of("Sale ID", "Date", "Product", "Quantity", "Total",
                "Customer", "Employee", "Status");
        rows.add(new String[]{"", "TOTAL", "", String.valueOf(units), money(revenue), "", "", ""});
        return new ReportTable("Sales report", period(from, to), headers, rows);
    }

    @Transactional(readOnly = true)
    public ReportTable inventory() {
        List<Smartphone> products = smartphoneRepository.findAll(
                Sort.by(Sort.Direction.ASC, "smartname"));
        List<String[]> rows = new ArrayList<>();
        BigDecimal stockValue = BigDecimal.ZERO;
        for (Smartphone product : products) {
            int quantity = product.getSmartqty() == null ? 0 : product.getSmartqty();
            BigDecimal price = product.getSmartprice() == null
                    ? BigDecimal.ZERO
                    : product.getSmartprice();
            stockValue = stockValue.add(price.multiply(BigDecimal.valueOf(quantity)));
            rows.add(new String[]{
                    text(product.getSmartid()),
                    text(product.getSmartname()),
                    String.valueOf(product.getSmartram()),
                    String.valueOf(product.getSmartstorage()),
                    money(price),
                    String.valueOf(quantity),
                    money(price.multiply(BigDecimal.valueOf(quantity))),
                    quantity <= 0 ? "OUT OF STOCK" : "IN STOCK"
            });
        }
        List<String> headers = List.of("SKU", "Product", "RAM (GB)", "Storage (GB)", "Price",
                "Stock", "Stock value", "Availability");
        rows.add(new String[]{"", "TOTAL", "", "", "", String.valueOf(totalStock(products)),
                money(stockValue), ""});
        return new ReportTable("Inventory report", LocalDate.now().toString(), headers, rows);
    }

    @Transactional(readOnly = true)
    public ReportTable inventoryHistory() {
        List<InventoryHistory> history = inventoryHistoryRepository.findAllByOrderByCreatedAtDesc();
        List<String[]> rows = new ArrayList<>();
        for (InventoryHistory entry : history) {
            rows.add(new String[]{
                    String.valueOf(entry.getHistoryid()),
                    text(entry.getSmartphone() == null ? "" : entry.getSmartphone().getSmartname()),
                    text(entry.getChangeType()),
                    String.valueOf(entry.getQuantityChange()),
                    String.valueOf(entry.getPreviousQty()),
                    String.valueOf(entry.getNewQty()),
                    text(entry.getNotes()),
                    text(entry.getChangedBy() == null ? "" : entry.getChangedBy().getEmpname()),
                    text(entry.getCreatedAt())
            });
        }
        List<String> headers = List.of("Movement", "Product", "Type", "Change", "From", "To",
                "Notes", "Changed by", "When");
        return new ReportTable("Stock movements", LocalDate.now().toString(), headers, rows);
    }

    /**
     * Purchase history per customer, including customers who never ordered.
     */
    @Transactional(readOnly = true)
    public ReportTable customers() {
        Map<Integer, List<Sale>> salesByCustomer = saleRepository.findAll().stream()
                .filter(sale -> sale.getCustomer() != null)
                .collect(Collectors.groupingBy(sale -> sale.getCustomer().getCustid()));
        Map<Integer, Long> ordersByCustomer = orderRepository.findAll().stream()
                .filter(order -> order.getCustomer() != null)
                .collect(Collectors.groupingBy(order -> order.getCustomer().getCustid(),
                        Collectors.counting()));

        List<String[]> rows = new ArrayList<>();
        List<Customer> customers = customerRepository.findAllByOrderByCustidAsc();
        for (Customer customer : customers) {
            List<Sale> sales = salesByCustomer.getOrDefault(customer.getCustid(), List.of());
            BigDecimal revenue = sales.stream()
                    .map(sale -> sale.getSaletotal() == null ? BigDecimal.ZERO : sale.getSaletotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int units = sales.stream().mapToInt(sale -> sale.getSaleqty() == null ? 0 : sale.getSaleqty())
                    .sum();
            rows.add(new String[]{
                    String.valueOf(customer.getCustid()),
                    text(customer.getCustname()),
                    text(customer.getEmail()),
                    text(customer.getCustpnum()),
                    String.valueOf(ordersByCustomer.getOrDefault(customer.getCustid(), 0L)),
                    String.valueOf(units),
                    money(revenue)
            });
        }
        List<String> headers = List.of("Customer ID", "Name", "Email", "Phone", "Orders",
                "Units bought", "Total spent");
        return new ReportTable("Customer purchase history", LocalDate.now().toString(),
                headers, rows);
    }

    /**
     * Employee performance with the commission owed for the period.
     */
    @Transactional(readOnly = true)
    public ReportTable employees() {
        Map<Integer, List<Sale>> salesByEmployee = saleRepository.findAll().stream()
                .filter(sale -> sale.getEmployee() != null)
                .collect(Collectors.groupingBy(sale -> sale.getEmployee().getEmpid()));
        List<String[]> rows = new ArrayList<>();
        for (Employee employee : employeeRepository.findAllByOrderByEmpidAsc()) {
            List<Sale> sales = salesByEmployee.getOrDefault(employee.getEmpid(), List.of());
            BigDecimal revenue = sales.stream()
                    .map(sale -> sale.getSaletotal() == null ? BigDecimal.ZERO : sale.getSaletotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int units = sales.stream().mapToInt(sale -> sale.getSaleqty() == null ? 0 : sale.getSaleqty())
                    .sum();
            BigDecimal commission = revenue.multiply(BigDecimal.valueOf(commissionRate))
                    .setScale(2, RoundingMode.HALF_UP);
            rows.add(new String[]{
                    String.valueOf(employee.getEmpid()),
                    text(employee.getEmpname()),
                    text(employee.getEmpuname()),
                    text(employee.getRole()),
                    String.valueOf(sales.size()),
                    String.valueOf(units),
                    money(revenue),
                    money(commission)
            });
        }
        List<String> headers = List.of("Employee ID", "Name", "Username", "Role", "Sales",
                "Units sold", "Revenue", "Commission");
        rows.add(new String[]{"", "", "", "", "", "", "",
                "Rate: " + commissionRate + " per 1.00 of revenue"});
        return new ReportTable("Employee performance and commission",
                LocalDate.now().toString(), headers, rows);
    }

    @Transactional(readOnly = true)
    public List<Sale> findSales(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return saleRepository.findAllByOrderBySaleDateDesc();
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
    }

    public byte[] toCsv(ReportTable table) {
        StringBuilder csv = new StringBuilder();
        csv.append(csv(table.title())).append('\n');
        csv.append(csv(table.period())).append('\n');
        csv.append(join(table.headers())).append('\n');
        for (String[] row : table.rows()) {
            csv.append(join(row)).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toXlsx(ReportTable table) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet(sheetName(table.title()));

            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            var titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            var totalStyle = workbook.createCellStyle();
            var totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalStyle.setUnderline(FontUnderline.SINGLE);

            sheet.createRow(0).createCell(0).setCellValue(table.title());
            sheet.getRow(0).setCellStyle(titleStyle);
            sheet.createRow(1).createCell(0).setCellValue(table.period());

            int headerRow = 3;
            var row = sheet.createRow(headerRow);
            for (int index = 0; index < table.headers().size(); index++) {
                var cell = row.createCell(index);
                cell.setCellValue(table.headers().get(index));
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = headerRow + 1;
            for (String[] values : table.rows()) {
                var dataRow = sheet.createRow(rowIndex++);
                for (int index = 0; index < values.length; index++) {
                    var cell = dataRow.createCell(index);
                    cell.setCellValue(values[index] == null ? "" : values[index]);
                    if (isTotalRow(values)) {
                        cell.setCellStyle(totalStyle);
                    }
                }
            }

            for (int index = 0; index < table.headers().size(); index++) {
                sheet.autoSizeColumn(index);
                sheet.setColumnWidth(index, Math.min(sheet.getColumnWidth(index) + 1024, 12000));
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build the XLSX report", ex);
        }
    }

    public byte[] toPdf(ReportTable table) {
        Document document = new Document(PageSize.A4.rotate(), 28, 28, 36, 28);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            document.add(new Paragraph(table.title(), titleFont));
            document.add(new Paragraph(table.period(), metaFont));
            document.add(new Paragraph(" "));

            PdfPTable pdfTable = new PdfPTable(table.headers().size());
            pdfTable.setWidthPercentage(100);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            for (String header : table.headers()) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfTable.addCell(cell);
            }
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            for (String[] values : table.rows()) {
                for (String value : values) {
                    pdfTable.addCell(new PdfPCell(new Phrase(value == null ? "" : value, bodyFont)));
                }
            }
            document.add(pdfTable);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to build the PDF report", ex);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return output.toByteArray();
    }

    public record ReportTable(String title, String period, List<String> headers,
                              List<String[]> rows) {
    }

    private String period(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return "All time · generated " + LocalDateTime.now().format(DATE_TIME);
        }
        return from.format(DATE) + " to " + to.format(DATE)
                + " · generated " + LocalDateTime.now().format(DATE_TIME);
    }

    private String join(String[] values) {
        StringBuilder line = new StringBuilder();
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                line.append(',');
            }
            line.append(csv(values[index]));
        }
        return line.toString();
    }

    private boolean isTotalRow(String[] values) {
        for (String value : values) {
            if ("TOTAL".equals(value)) {
                return true;
            }
        }
        return false;
    }

    private String sheetName(String title) {
        String cleaned = title.replaceAll("[\\[\\]:*?/\\\\]", " ").trim();
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    private int totalStock(List<Smartphone> products) {
        return products.stream()
                .mapToInt(product -> product.getSmartqty() == null ? 0 : product.getSmartqty())
                .sum();
    }

    private String text(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private String money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}