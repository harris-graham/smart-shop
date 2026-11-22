package com.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer reviewid;
    private String smartid;
    private String smartname;
    private String customerName;
    private Integer rating;
    private String comment;
    private String status;
    private LocalDateTime createdAt;
}
