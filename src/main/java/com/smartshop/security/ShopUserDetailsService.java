package com.smartshop.security;

import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return employeeRepository.findByEmpunameIgnoreCase(username)
                .map(this::toUserDetails)
                .orElseGet(() -> customerRepository.findByEmailIgnoreCase(username)
                        .filter(customer -> customer.getPassword() != null
                                && !customer.getPassword().isBlank())
                        .map(this::toUserDetails)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "No account found for " + username)));
    }

    private UserDetails toUserDetails(Employee employee) {
        String role = employee.getRole() == null ? "EMPLOYEE" : employee.getRole().toUpperCase();
        return User.withUsername(employee.getEmpuname())
                .password(employee.getEmppword())
                .roles(role)
                .build();
    }

    private UserDetails toUserDetails(Customer customer) {
        return User.withUsername(customer.getEmail())
                .password(customer.getPassword())
                .roles("CUSTOMER")
                .build();
    }
}
