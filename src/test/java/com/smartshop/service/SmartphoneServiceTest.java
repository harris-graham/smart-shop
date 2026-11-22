package com.smartshop.service;

import com.smartshop.dto.SmartphoneForm;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.SmartphoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartphoneServiceTest {

    @Mock
    private SmartphoneRepository smartphoneRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private SmartphoneService smartphoneService;

    @Test
    void createAssignsIdAndSavesProduct() {
        when(smartphoneRepository.save(any(Smartphone.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SmartphoneForm form = form("Pixel 9", "799.00", 10);

        Smartphone created = smartphoneService.create(form, null);

        assertThat(created.getSmartid()).startsWith("SP-");
        assertThat(created.getSmartname()).isEqualTo("Pixel 9");
        assertThat(created.getSmartprice()).isEqualByComparingTo("799.00");
        verify(smartphoneRepository).save(any(Smartphone.class));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(smartphoneRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> smartphoneService.findById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addStockDelegatesToInventory() {
        Smartphone smartphone = new Smartphone("SP-1", "Phone", BigDecimal.TEN, 2, 8, 128, null);
        when(inventoryService.restock("SP-1", 5, null)).thenReturn(smartphone);

        Smartphone result = smartphoneService.addStock("SP-1", 5, null);

        assertThat(result).isSameAs(smartphone);
        verify(inventoryService).restock("SP-1", 5, null);
    }

    @Test
    void bulkPriceChangeAppliesPercentageToScope() {
        Smartphone budget = new Smartphone("SP-1", "Budget", new BigDecimal("100.00"), 1, 4, 64, null);
        Smartphone flagship = new Smartphone("SP-2", "Flagship", new BigDecimal("1000.00"), 20, 12, 256, null);

        when(smartphoneRepository.findBySmartqtyGreaterThanAndSmartqtyLessThanEqualOrderBySmartqtyAsc(0, 3))
                .thenReturn(List.of(budget));
        when(smartphoneRepository.save(any(Smartphone.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int changed = smartphoneService.bulkPriceChange("PERCENT_UP", new BigDecimal("10"), "LOW_STOCK", 3);

        assertThat(changed).isEqualTo(1);
        assertThat(budget.getSmartprice()).isEqualByComparingTo("110.00");
        assertThat(flagship.getSmartprice()).isEqualByComparingTo("1000.00");
    }

    @Test
    void importCsvCreatesAndUpdatesProducts() {
        String csv = """
                smartid,smartname,smartprice,smartqty,smartram,smartstorage
                SP-1,Phone One,199.00,4,6,128
                SP-2,Phone Two,299.00,7,8,256
                """;
        Smartphone existing = new Smartphone("SP-1", "Old name", new BigDecimal("150.00"), 1, 4, 64, null);

        when(smartphoneRepository.findById("SP-1")).thenReturn(Optional.of(existing));
        when(smartphoneRepository.findById("SP-2")).thenReturn(Optional.empty());
        when(smartphoneRepository.save(any(Smartphone.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SmartphoneService.ImportResult result = smartphoneService.importCsv(
                new MockMultipartFile("file", "products.csv", "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)));

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.errors()).isEmpty();
        assertThat(existing.getSmartname()).isEqualTo("Phone One");
        assertThat(existing.getSmartprice()).isEqualByComparingTo("199.00");
    }

    private SmartphoneForm form(String name, String price, int quantity) {
        SmartphoneForm form = new SmartphoneForm();
        form.setSmartname(name);
        form.setSmartprice(new BigDecimal(price));
        form.setSmartqty(quantity);
        form.setSmartram(8);
        form.setSmartstorage(128);
        return form;
    }
}
