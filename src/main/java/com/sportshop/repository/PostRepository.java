package com.sportshop.repository;

import com.sportshop.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @Query("SELECT p FROM PostEntity p WHERE (:search IS NULL OR lower(p.title) LIKE lower(concat('%', :search, '%')) OR lower(p.category.name) LIKE lower(concat('%', :search, '%')))")
    Page<PostEntity> findWithSearch(@Param("search") String search, Pageable pageable);

    Optional<PostEntity> findBySlug(String slug);

    @Query("SELECT p FROM PostEntity p WHERE p.category.name = :categoryName AND (:search IS NULL OR lower(p.title) LIKE lower(concat('%', :search, '%')))")
    Page<PostEntity> findByCategoryAndSearch(@Param("categoryName") String categoryName, @Param("search") String search, Pageable pageable);
}
