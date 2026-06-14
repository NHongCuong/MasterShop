package com.sportshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Integer rating;
    private String comment;
    private Date createdAt;
    private Date updatedAt;
}
