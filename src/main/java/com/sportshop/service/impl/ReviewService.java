package com.sportshop.service.impl;

import com.sportshop.dto.ReviewDTO;
import com.sportshop.entity.ProductEntity;
import com.sportshop.entity.ReviewEntity;
import com.sportshop.entity.UserEntity;
import com.sportshop.repository.ProductRepository;
import com.sportshop.repository.ReviewRepository;
import com.sportshop.repository.UserRepository;
import com.sportshop.service.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService implements IReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        ReviewEntity entity = new ReviewEntity();
        UserEntity user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProductEntity product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        entity.setUser(user);
        entity.setProduct(product);
        entity.setRating(reviewDTO.getRating() != null ? reviewDTO.getRating() : 5);
        entity.setComment(reviewDTO.getComment());

        ReviewEntity saved = reviewRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (reviewDTO.getRating() != null) {
            entity.setRating(reviewDTO.getRating());
        }
        if (reviewDTO.getComment() != null) {
            entity.setComment(reviewDTO.getComment());
        }
        
        ReviewEntity updated = reviewRepository.save(entity);
        return mapToDTO(updated);
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    private ReviewDTO mapToDTO(ReviewEntity entity) {
        return ReviewDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getNameUser() : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .rating(entity.getRating())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
