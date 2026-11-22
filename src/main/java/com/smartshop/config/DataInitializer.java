package com.smartshop.config;

import com.smartshop.entity.Employee;
import com.smartshop.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Makes sure the demo administrator exists and that the password shipped in the
 * database migration can actually be used. The migration seeds a fixed BCrypt
 * hash; if that hash does not match the documented password it is replaced once
 * with a freshly encoded value.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String SEEDED_ADMIN_HASH =
            "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5NXyZ3O0/5HCS";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        employeeRepository.findByEmpunameIgnoreCase(ADMIN_USERNAME)
                .ifPresentOrElse(this::repairSeededPassword, this::createDefaultAdmin);
    }

    private void repairSeededPassword(Employee admin) {
        if (!SEEDED_ADMIN_HASH.equals(admin.getEmppword())) {
            return;
        }
        if (passwordEncoder.matches(DEFAULT_ADMIN_PASSWORD, admin.getEmppword())) {
            return;
        }
        admin.setEmppword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        employeeRepository.save(admin);
        log.info("Updated the seeded administrator password to the documented demo value");
    }

    private void createDefaultAdmin() {
        Employee admin = new Employee();
        admin.setEmpname("Admin");
        admin.setEmppnum("0000000000");
        admin.setEmpaddress("Admin Address");
        admin.setEmpuname(ADMIN_USERNAME);
        admin.setEmppword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setRole("ADMIN");
        employeeRepository.save(admin);
        log.info("Created the default administrator account '{}'", ADMIN_USERNAME);
    }
}
