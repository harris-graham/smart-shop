package com.smartshop.service;

import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.EmployeeRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ThemeService {

    public static final String COOKIE_NAME = "smartshop-theme";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public String resolve(HttpServletRequest request) {
        String fromCookie = readCookie(request);
        if (DARK.equals(fromCookie) || LIGHT.equals(fromCookie)) {
            return fromCookie;
        }
        return null;
    }

    /**
     * Applies the requested theme to the browser cookie and, when the visitor is
     * signed in, to the stored profile so the choice survives on other devices.
     */
    @Transactional
    public String apply(String theme, String username, HttpServletRequest request,
                        HttpServletResponse response) {
        String normalised = normalise(theme);
        if (normalised == null) {
            return resolve(request);
        }
        if (username != null && !username.isBlank()) {
            customerRepository.findByEmailIgnoreCase(username)
                    .ifPresent(customer -> {
                        customer.setTheme(normalised);
                        customerRepository.save(customer);
                    });
            employeeRepository.findByEmpunameIgnoreCase(username)
                    .ifPresent(employee -> {
                        employee.setTheme(normalised);
                        employeeRepository.save(employee);
                    });
        }
        Cookie cookie = new Cookie(COOKIE_NAME, normalised);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
        return normalised;
    }

    @Transactional(readOnly = true)
    public String preferred(String username) {
        if (username == null || username.isBlank()) {
            return LIGHT;
        }
        Optional<String> customer = customerRepository.findByEmailIgnoreCase(username)
                .map(value -> value.getTheme());
        if (customer.isPresent()) {
            return normalise(customer.orElse(LIGHT));
        }
        return employeeRepository.findByEmpunameIgnoreCase(username)
                .map(value -> normalise(value.getTheme()))
                .orElse(LIGHT);
    }

    private String readCookie(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String normalise(String theme) {
        if (theme == null) {
            return null;
        }
        String value = theme.trim().toLowerCase(Locale.ROOT);
        if (DARK.equals(value) || LIGHT.equals(value)) {
            return value;
        }
        return null;
    }
}