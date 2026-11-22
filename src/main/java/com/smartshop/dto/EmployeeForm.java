package com.smartshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeForm {

    private Integer empid;

    @NotBlank
    @Size(max = 60)
    private String empname;

    @Pattern(regexp = "^[0-9]{0,20}$", message = "Phone number must contain digits only")
    private String emppnum;

    @Size(max = 100)
    private String empaddress;

    @NotBlank
    @Size(max = 20)
    private String empuname;

    @Size(max = 60)
    private String emppword;

    private String role = "EMPLOYEE";
}
