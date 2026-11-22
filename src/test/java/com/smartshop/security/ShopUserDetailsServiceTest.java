package com.smartshop.security;

import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopUserDetailsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ShopUserDetailsService userDetailsService;

    @Test
    void loadsEmployeeWithRole() {
        Employee employee = new Employee();
        employee.setEmpuname("admin");
        employee.setEmppword("$2a$10$hash");
        employee.setRole("ADMIN");
        when(employeeRepository.findByEmpunameIgnoreCase("admin")).thenReturn(Optional.of(employee));

        UserDetails details = userDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadsCustomerAccount() {
        Customer customer = new Customer();
        customer.setEmail("alice@example.com");
        customer.setPassword("$2a$10$hash");
        when(employeeRepository.findByEmpunameIgnoreCase("alice@example.com"))
                .thenReturn(Optional.empty());
        when(customerRepository.findByEmailIgnoreCase("alice@example.com"))
                .thenReturn(Optional.of(customer));

        UserDetails details = userDetailsService.loadUserByUsername("alice@example.com");

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void rejectsUnknownAccount() {
        when(employeeRepository.findByEmpunameIgnoreCase("ghost")).thenReturn(Optional.empty());
        when(customerRepository.findByEmailIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
