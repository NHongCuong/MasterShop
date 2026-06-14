package com.sportshop.service;

import com.sportshop.dto.ReviewDTO;
import java.util.List;

public interface IReviewService {
    List<ReviewDTO> getAllReviews();
    List<ReviewDTO> getReviewsByProductId(Long productId);
    List<ReviewDTO> getReviewsByUserId(Long userId);
    ReviewDTO createReview(ReviewDTO reviewDTO);
    ReviewDTO updateReview(Long id, ReviewDTO reviewDTO);
    void deleteReview(Long id);
}
