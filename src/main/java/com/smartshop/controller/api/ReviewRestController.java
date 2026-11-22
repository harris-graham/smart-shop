package com.smartshop.controller.api;

import com.smartshop.dto.ReviewDTO;
import com.smartshop.dto.ReviewForm;
import com.smartshop.dto.StatusRequest;
import com.smartshop.entity.Review;
import com.smartshop.service.ReviewService;
import com.smartshop.service.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewRestController {

    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    @GetMapping("/products/{smartid}/reviews")
    public List<ReviewDTO> list(@PathVariable String smartid) {
        return reviewService.approvedForProduct(smartid).stream()
                .map(reviewService::toDTO)
                .toList();
    }

    @PostMapping("/products/{smartid}/reviews")
    public ReviewDTO create(@PathVariable String smartid, @Valid @RequestBody ReviewForm form) {
        form.setSmartid(smartid);
        Review review = reviewService.addReview(securityUtils.currentUsername().orElseThrow(), form);
        return reviewService.toDTO(review);
    }

    @PutMapping("/reviews/{reviewid}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ReviewDTO moderate(@PathVariable Integer reviewid,
                              @Valid @RequestBody StatusRequest request) {
        return reviewService.toDTO(reviewService.moderate(reviewid, request.getStatus()));
    }
}
