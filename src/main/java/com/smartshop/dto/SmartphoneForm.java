package com.smartshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmartphoneForm {

    private String smartid;

    @NotBlank
    @Size(max = 50)
    private String smartname;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal smartprice;

    @NotNull
    @Min(0)
    private Integer smartqty;

    @NotNull
    @Min(0)
    private Integer smartram;

    @NotNull
    @Min(0)
    private Integer smartstorage;

    /** Optional Base64 image payload for API clients. */
    private String imageBase64;
}
