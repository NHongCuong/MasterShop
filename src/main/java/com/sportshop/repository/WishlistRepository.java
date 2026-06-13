package com.sportshop.repository;

import com.sportshop.entity.WishlistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

    @Query("SELECT w FROM WishlistEntity w " +
            "WHERE (:search IS NULL OR lower(w.user.nameUser) LIKE lower(concat('%', :search, '%')) OR lower(w.product.name) LIKE lower(concat('%', :search, '%')))")
    Page<WishlistEntity> findWithSearch(@Param("search") String search, Pageable pageable);

    Optional<WishlistEntity> findByUserIdAndProductId(Long userId, Long productId);

    List<WishlistEntity> findByUserId(Long userId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
