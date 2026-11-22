package com.smartshop.service;

import com.smartshop.dto.SmartphoneForm;
import com.smartshop.entity.Employee;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.SmartphoneRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SmartphoneService {

    private final SmartphoneRepository smartphoneRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<Smartphone> findAll() {
        return smartphoneRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Smartphone> findAll(Pageable pageable) {
        return smartphoneRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Smartphone> search(String keyword, BigDecimal minPrice, BigDecimal maxPrice,
                                   Integer minRam, Integer minStorage, Pageable pageable) {
        Specification<Smartphone> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(cb.lower(root.get("smartname")),
                        "%" + keyword.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("smartprice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("smartprice"), maxPrice));
            }
            if (minRam != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("smartram"), minRam));
            }
            if (minStorage != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("smartstorage"), minStorage));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return smartphoneRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public Smartphone findById(String smartid) {
        return smartphoneRepository.findById(smartid)
                .orElseThrow(() -> new ResourceNotFoundException("Smartphone", "id", smartid));
    }

    @Transactional
    public Smartphone create(SmartphoneForm form, MultipartFile image) {
        Smartphone smartphone = new Smartphone();
        smartphone.setSmartid(StringUtils.hasText(form.getSmartid())
                ? form.getSmartid()
                : "SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        apply(smartphone, form);
        String encoded = readImage(image);
        if (encoded == null && StringUtils.hasText(form.getImageBase64())) {
            encoded = form.getImageBase64();
        }
        smartphone.setSmartimg(encoded);
        return smartphoneRepository.save(smartphone);
    }

    @Transactional
    public Smartphone update(String smartid, SmartphoneForm form, MultipartFile image) {
        Smartphone smartphone = findById(smartid);
        apply(smartphone, form);
        String encoded = readImage(image);
        if (encoded == null && StringUtils.hasText(form.getImageBase64())) {
            encoded = form.getImageBase64();
        }
        if (encoded != null) {
            smartphone.setSmartimg(encoded);
        }
        return smartphoneRepository.save(smartphone);
    }

    @Transactional
    public Smartphone addStock(String smartid, int quantity, Employee changedBy) {
        return inventoryService.restock(smartid, quantity, changedBy);
    }

    /**
     * Applies a percentage or fixed price change to every product in scope.
     *
     * @param mode  {@code PERCENT_UP}, {@code PERCENT_DOWN} or {@code FIXED}
     * @param scope {@code ALL}, {@code LOW_STOCK} or {@code OUT_OF_STOCK}
     * @return the number of products that changed
     */
    @Transactional
    public int bulkPriceChange(String mode, BigDecimal value, String scope, int lowStockThreshold) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException("Enter a price adjustment greater than zero");
        }
        String action = mode == null ? "" : mode.trim().toUpperCase(Locale.ROOT);
        List<Smartphone> targets = switch (scope == null ? "ALL" : scope.trim().toUpperCase(Locale.ROOT)) {
            case "LOW_STOCK" -> smartphoneRepository
                    .findBySmartqtyGreaterThanAndSmartqtyLessThanEqualOrderBySmartqtyAsc(0, lowStockThreshold);
            case "OUT_OF_STOCK" -> smartphoneRepository.findBySmartqty(0);
            default -> smartphoneRepository.findAll(Sort.by(Sort.Direction.ASC, "smartid"));
        };
        int changed = 0;
        for (Smartphone smartphone : targets) {
            BigDecimal price = smartphone.getSmartprice() == null
                    ? BigDecimal.ZERO
                    : smartphone.getSmartprice();
            BigDecimal updated = switch (action) {
                case "PERCENT_UP" -> price.add(price.multiply(value)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                case "PERCENT_DOWN" -> price.subtract(price.multiply(value)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                case "FIXED" -> price.add(value);
                default -> throw new IllegalArgumentException("Unknown price adjustment mode");
            };
            if (updated.signum() < 0) {
                updated = BigDecimal.ZERO;
            }
            if (updated.compareTo(price) == 0) {
                continue;
            }
            smartphone.setSmartprice(updated.setScale(2, RoundingMode.HALF_UP));
            smartphoneRepository.save(smartphone);
            changed++;
        }
        return changed;
    }

    /**
     * Imports products from a CSV file. Expected header:
     * <pre>smartid,smartname,smartprice,smartqty,smartram,smartstorage</pre>
     * Existing rows are updated by {@code smartid}; new rows are created.
     */
    @Transactional
    public ImportResult importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a CSV file to import");
        }
        List<String> lines;
        try {
            lines = new BufferedReader(new InputStreamReader(file.getInputStream(),
                    StandardCharsets.UTF_8)).lines().toList();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read the uploaded file", ex);
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("The CSV file is empty");
        }
        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();
        int start = looksLikeHeader(lines.get(0)) ? 1 : 0;
        for (int index = start; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                String[] columns = split(line);
                if (columns.length < 6) {
                    errors.add("Line " + (index + 1) + ": expected 6 columns");
                    continue;
                }
                String smartid = columns[0].isBlank() ? null : columns[0].trim();
                String name = columns[1].trim();
                BigDecimal price = new BigDecimal(columns[2].trim());
                int quantity = Integer.parseInt(columns[3].trim());
                int ram = Integer.parseInt(columns[4].trim());
                int storage = Integer.parseInt(columns[5].trim());

                Smartphone smartphone = smartid == null
                        ? null
                        : smartphoneRepository.findById(smartid).orElse(null);
                boolean isNew = smartphone == null;
                if (smartphone == null) {
                    smartphone = new Smartphone();
                    smartphone.setSmartid(smartid == null
                            ? "SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                            : smartid);
                    smartphone.setSmartimg(null);
                }
                smartphone.setSmartname(name);
                smartphone.setSmartprice(price);
                smartphone.setSmartqty(quantity);
                smartphone.setSmartram(ram);
                smartphone.setSmartstorage(storage);
                smartphoneRepository.save(smartphone);
                if (isNew) {
                    created++;
                } else {
                    updated++;
                }
            } catch (RuntimeException ex) {
                errors.add("Line " + (index + 1) + ": " + ex.getMessage());
            }
        }
        return new ImportResult(created, updated, errors);
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        StringBuilder csv = new StringBuilder(
                "smartid,smartname,smartprice,smartqty,smartram,smartstorage\n");
        for (Smartphone smartphone : smartphoneRepository.findAll(
                Sort.by(Sort.Direction.ASC, "smartid"))) {
            csv.append(csv(smartphone.getSmartid())).append(',')
                    .append(csv(smartphone.getSmartname())).append(',')
                    .append(smartphone.getSmartprice() == null ? "0" : smartphone.getSmartprice())
                    .append(',')
                    .append(smartphone.getSmartqty() == null ? 0 : smartphone.getSmartqty())
                    .append(',')
                    .append(smartphone.getSmartram() == null ? 0 : smartphone.getSmartram())
                    .append(',')
                    .append(smartphone.getSmartstorage() == null ? 0 : smartphone.getSmartstorage())
                    .append('\n');
        }
        return csv.toString();
    }

    public record ImportResult(int created, int updated, List<String> errors) {
    }

    private boolean looksLikeHeader(String line) {
        return line != null && line.toLowerCase(Locale.ROOT).startsWith("smartid");
    }

    private String[] split(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                quoted = !quoted;
            } else if (character == ',' && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        columns.add(current.toString());
        return columns.toArray(new String[0]);
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        if (value.contains(",") || value.contains("\"")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    @Transactional
    public void delete(String smartid) {
        Smartphone smartphone = findById(smartid);
        smartphoneRepository.delete(smartphone);
    }

    @Transactional(readOnly = true)
    public List<Smartphone> lowStock(int threshold) {
        return inventoryService.lowStock(threshold);
    }

    private void apply(Smartphone smartphone, SmartphoneForm form) {
        smartphone.setSmartname(form.getSmartname());
        smartphone.setSmartprice(form.getSmartprice() == null ? BigDecimal.ZERO : form.getSmartprice());
        smartphone.setSmartqty(form.getSmartqty() == null ? 0 : form.getSmartqty());
        smartphone.setSmartram(form.getSmartram() == null ? 0 : form.getSmartram());
        smartphone.setSmartstorage(form.getSmartstorage() == null ? 0 : form.getSmartstorage());
    }

    private String readImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(image.getBytes());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read uploaded image", ex);
        }
    }
}
