package com.smartshop.controller.api;

import com.smartshop.dto.LoginRequest;
import com.smartshop.dto.LoginResponse;
import com.smartshop.dto.RegisterForm;
import com.smartshop.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request);
        if (response.isSuccess()) {
            persistSession(httpRequest, httpResponse);
        }
        return response;
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterForm form) {
        return authService.register(form);
    }

    @GetMapping("/me")
    public LoginResponse me() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || !context.getAuthentication().isAuthenticated()) {
            return new LoginResponse(null, null, null, null, false, "Not signed in");
        }
        String role = context.getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        return new LoginResponse(null, null, context.getAuthentication().getName(),
                role, true, "Signed in");
    }

    private void persistSession(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.getContext();
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
