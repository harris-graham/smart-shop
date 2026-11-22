package com.smartshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerForm {

    private Integer custid;

    @NotBlank
    @Size(max = 60)
    private String custname;

    @Pattern(regexp = "^[0-9]{0,20}$", message = "Phone number must contain digits only")
    private String custpnum;

    @Size(max = 100)
    private String custaddress;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 60)
    private String password;
}
