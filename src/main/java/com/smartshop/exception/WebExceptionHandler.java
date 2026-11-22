package com.smartshop.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView notFound(ResourceNotFoundException ex) {
        return view(HttpStatus.NOT_FOUND, "error/404", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView missingResource(NoResourceFoundException ex) {
        return view(HttpStatus.NOT_FOUND, "error/404", "The requested page does not exist");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView forbidden(AccessDeniedException ex) {
        return view(HttpStatus.FORBIDDEN, "error/403",
                ex.getMessage() == null ? "You do not have permission to view this page" : ex.getMessage());
    }

    @ExceptionHandler({InsufficientStockException.class, IllegalArgumentException.class,
            IllegalStateException.class})
    public ModelAndView badRequest(RuntimeException ex) {
        return view(HttpStatus.BAD_REQUEST, "error/400", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView unexpected(Exception ex) {
        return view(HttpStatus.INTERNAL_SERVER_ERROR, "error/500",
                "An unexpected error occurred");
    }

    private ModelAndView view(HttpStatus status, String template, String message) {
        ModelAndView modelAndView = new ModelAndView(template);
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", status.getReasonPhrase());
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
