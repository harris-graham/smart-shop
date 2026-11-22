package com.smartshop.controller;

import com.smartshop.service.SecurityUtils;
import com.smartshop.service.ThemeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@Controller
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;
    private final SecurityUtils securityUtils;

    @PostMapping("/theme")
    public String switchTheme(@RequestParam String theme,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        String resolved = themeService.apply(theme,
                securityUtils.currentUsername().orElse(null), request, response);
        redirectAttributes.addFlashAttribute("message",
                (ThemeService.DARK.equals(resolved) ? "Dark" : "Light") + " theme enabled");
        return "redirect:" + relativeRedirect(request.getHeader("Referer"));
    }

    private String relativeRedirect(String referer) {
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        try {
            URI uri = URI.create(referer);
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (!path.startsWith("/") || path.startsWith("//")) {
                return "/";
            }
            String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
            return path + query;
        } catch (IllegalArgumentException ex) {
            return "/";
        }
    }
}