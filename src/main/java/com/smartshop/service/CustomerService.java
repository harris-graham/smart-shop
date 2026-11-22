package com.smartshop.service;

import com.smartshop.dto.CustomerForm;
import com.smartshop.dto.RegisterForm;
import com.smartshop.entity.Customer;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        List<Customer> customers = customerRepository.findAllByOrderByCustidAsc();
        enrich(customers);
        return customers;
    }

    @Transactional(readOnly = true)
    public List<Customer> search(String query) {
        if (!StringUtils.hasText(query)) {
            return findAll();
        }
        List<Customer> customers = customerRepository.search(query);
        enrich(customers);
        return customers;
    }

    @Transactional(readOnly = true)
    public Customer findById(Integer custid) {
        return customerRepository.findById(custid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", custid));
    }

    @Transactional(readOnly = true)
    public Customer findByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    @Transactional
    public Customer create(CustomerForm form) {
        Customer customer = new Customer();
        apply(customer, form);
        if (StringUtils.hasText(form.getPassword())) {
            customer.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Integer custid, CustomerForm form) {
        Customer customer = findById(custid);
        apply(customer, form);
        if (StringUtils.hasText(form.getPassword())) {
            customer.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer register(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (customerRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        Customer customer = new Customer();
        customer.setCustname(form.getName());
        customer.setEmail(form.getEmail().trim().toLowerCase());
        customer.setCustpnum(form.getPhone());
        customer.setCustaddress(form.getAddress());
        customer.setPassword(passwordEncoder.encode(form.getPassword()));
        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Integer custid) {
        Customer customer = findById(custid);
        customerRepository.delete(customer);
    }

    private void apply(Customer customer, CustomerForm form) {
        customer.setCustname(form.getCustname());
        customer.setCustpnum(form.getCustpnum());
        customer.setCustaddress(form.getCustaddress());
        if (StringUtils.hasText(form.getEmail())) {
            customer.setEmail(form.getEmail().trim().toLowerCase());
        }
    }

    private void enrich(List<Customer> customers) {
        Map<Integer, Integer> purchases = new HashMap<>();
        for (Object[] row : saleRepository.findCustomerPerformance(
                org.springframework.data.domain.PageRequest.of(0, 1000))) {
            purchases.put((Integer) row[0], ((Number) row[2]).intValue());
        }
        customers.forEach(customer ->
                customer.setSalesum(purchases.getOrDefault(customer.getCustid(), 0)));
    }
}
