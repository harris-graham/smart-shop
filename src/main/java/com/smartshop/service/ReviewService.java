package com.smartshop.service;

import com.smartshop.dto.ReviewDTO;
import com.smartshop.dto.ReviewForm;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Review;
import com.smartshop.entity.Smartphone;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.ReviewRepository;
import com.smartshop.repository.SmartphoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReviewService {

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private final ReviewRepository reviewRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public Review addReview(String username, ReviewForm form) {
        Customer customer = customerRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException("Only signed-in customers can review products"));
        Smartphone smartphone = smartphoneRepository.findById(form.getSmartid())
                .orElseThrow(() -> new ResourceNotFoundException("Smartphone", "id", form.getSmartid()));
        reviewRepository.findBySmartphone_SmartidAndCustomer_Custid(form.getSmartid(), customer.getCustid())
                .ifPresent(existing -> {
                    throw new IllegalStateException("You have already reviewed this product");
                });
        Review review = new Review(smartphone, customer, form.getRating(), form.getComment());
        review.setStatus(PENDING);
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> approvedForProduct(String smartid) {
        return reviewRepository.findBySmartphone_SmartidAndStatusOrderByCreatedAtDesc(smartid, APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Review> pending() {
        return reviewRepository.findByStatusOrderByCreatedAtDesc(PENDING);
    }

    @Transactional(readOnly = true)
    public List<Review> findAll() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public double averageRating(String smartid) {
        Double average = reviewRepository.averageRating(smartid);
        return average == null ? 0.0 : Math.round(average * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public long approvedCount(String smartid) {
        return reviewRepository.countBySmartphone_SmartidAndStatus(smartid, APPROVED);
    }

    @Transactional(readOnly = true)
    public long pendingCount() {
        return reviewRepository.countByStatus(PENDING);
    }

    @Transactional
    public Review moderate(Integer reviewid, String status) {
        Review review = reviewRepository.findById(reviewid)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewid));
        String target = status == null ? "" : status.toUpperCase(Locale.ROOT);
        if (!APPROVED.equals(target) && !REJECTED.equals(target) && !PENDING.equals(target)) {
            throw new IllegalArgumentException("Unsupported review status: " + status);
        }
        review.setStatus(target);
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public ReviewDTO toDTO(Review review) {
        return ReviewDTO.builder()
                .reviewid(review.getReviewid())
                .smartid(review.getSmartphone() == null ? null : review.getSmartphone().getSmartid())
                .smartname(review.getSmartphone() == null ? null : review.getSmartphone().getSmartname())
                .customerName(review.getCustomer() == null ? null : review.getCustomer().getCustname())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
