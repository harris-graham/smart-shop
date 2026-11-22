package com.smartshop.service;

import com.smartshop.dto.LoginRequest;
import com.smartshop.dto.LoginResponse;
import com.smartshop.dto.RegisterForm;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .findFirst()
                    .orElse("USER");

            Employee employee = employeeRepository.findByEmpunameIgnoreCase(request.getUsername())
                    .orElse(null);
            if (employee != null) {
                return new LoginResponse(employee.getEmpid(), employee.getEmpname(),
                        employee.getEmpuname(), role, true, "Signed in");
            }
            Customer customer = customerRepository.findByEmailIgnoreCase(request.getUsername())
                    .orElse(null);
            return new LoginResponse(null,
                    customer == null ? request.getUsername() : customer.getCustname(),
                    request.getUsername(), role, true, "Signed in");
        } catch (org.springframework.security.core.AuthenticationException ex) {
            return new LoginResponse(null, null, request.getUsername(), null, false,
                    "Invalid username or password");
        }
    }

    public LoginResponse register(RegisterForm form) {
        try {
            Customer customer = customerService.register(form);
            return new LoginResponse(null, customer.getCustname(), customer.getEmail(),
                    "CUSTOMER", true, "Account created");
        } catch (IllegalArgumentException ex) {
            return new LoginResponse(null, null, form.getEmail(), null, false, ex.getMessage());
        }
    }
}
