package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Integer empid;
    private String empname;
    private String empuname;
    private String role;
    private boolean success;
    private String message;
}
