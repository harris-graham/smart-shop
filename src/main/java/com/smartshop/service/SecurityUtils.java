package com.smartshop.service;

import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public Optional<String> currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    public Optional<Customer> currentCustomer() {
        return currentUsername().flatMap(customerRepository::findByEmailIgnoreCase);
    }

    public Optional<Employee> currentEmployee() {
        return currentUsername().flatMap(employeeRepository::findByEmpunameIgnoreCase);
    }

    public boolean isCustomer() {
        return currentCustomer().isPresent();
    }

    public boolean isEmployee() {
        return currentEmployee().isPresent();
    }

    public boolean isAdmin() {
        return currentEmployee()
                .map(employee -> "ADMIN".equalsIgnoreCase(employee.getRole()))
                .orElse(false);
    }

    public String displayName() {
        return currentCustomer().map(Customer::getCustname)
                .or(() -> currentEmployee().map(Employee::getEmpname))
                .orElse("Guest");
    }
}
