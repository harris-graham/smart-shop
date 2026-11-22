package com.smartshop.service;

import com.smartshop.dto.EmployeeForm;
import com.smartshop.entity.Employee;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.EmployeeRepository;
import com.smartshop.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        List<Employee> employees = employeeRepository.findAllByOrderByEmpidAsc();
        Map<Integer, Integer> sales = new HashMap<>();
        for (Object[] row : saleRepository.findEmployeePerformance()) {
            sales.put((Integer) row[0], ((Number) row[2]).intValue());
        }
        employees.forEach(employee -> employee.setSalesum(sales.getOrDefault(employee.getEmpid(), 0)));
        return employees;
    }

    @Transactional(readOnly = true)
    public List<Employee> search(String query) {
        if (!StringUtils.hasText(query)) {
            return findAll();
        }
        List<Employee> employees = employeeRepository.search(query);
        Map<Integer, Integer> sales = new HashMap<>();
        for (Object[] row : saleRepository.findEmployeePerformance()) {
            sales.put((Integer) row[0], ((Number) row[2]).intValue());
        }
        employees.forEach(employee -> employee.setSalesum(sales.getOrDefault(employee.getEmpid(), 0)));
        return employees;
    }

    @Transactional(readOnly = true)
    public Employee findById(Integer empid) {
        return employeeRepository.findById(empid)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empid));
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByEmpunameIgnoreCase(username);
    }

    @Transactional
    public Employee create(EmployeeForm form) {
        if (employeeRepository.existsByEmpunameIgnoreCase(form.getEmpuname())) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (!StringUtils.hasText(form.getEmppword())) {
            throw new IllegalArgumentException("Password is required for a new employee");
        }
        Employee employee = new Employee();
        apply(employee, form);
        employee.setEmppword(passwordEncoder.encode(form.getEmppword()));
        employee.setRole(normalizeRole(form.getRole()));
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Integer empid, EmployeeForm form) {
        Employee employee = findById(empid);
        apply(employee, form);
        if (StringUtils.hasText(form.getEmppword())) {
            employee.setEmppword(passwordEncoder.encode(form.getEmppword()));
        }
        employee.setRole(normalizeRole(form.getRole()));
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateAccount(Integer empid, String username, String password) {
        Employee employee = findById(empid);
        if (StringUtils.hasText(username) && !username.equalsIgnoreCase(employee.getEmpuname())) {
            if (employeeRepository.existsByEmpunameIgnoreCase(username)) {
                throw new IllegalArgumentException("Username is already in use");
            }
            employee.setEmpuname(username);
        }
        if (StringUtils.hasText(password)) {
            employee.setEmppword(passwordEncoder.encode(password));
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(Integer empid) {
        Employee employee = findById(empid);
        if ("ADMIN".equalsIgnoreCase(employee.getRole())) {
            throw new IllegalArgumentException("The administrator account cannot be deleted");
        }
        employeeRepository.delete(employee);
    }

    private void apply(Employee employee, EmployeeForm form) {
        employee.setEmpname(form.getEmpname());
        employee.setEmppnum(form.getEmppnum());
        employee.setEmpaddress(form.getEmpaddress());
        employee.setEmpuname(form.getEmpuname());
    }

    private String normalizeRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "ADMIN";
        }
        return "EMPLOYEE";
    }
}
