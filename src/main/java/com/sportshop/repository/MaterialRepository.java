package com.sportshop.repository;

import com.sportshop.entity.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {
    @Query("SELECT m FROM MaterialEntity m " +
           "WHERE (:search IS NULL OR m.nameMaterial LIKE %:search% OR CAST(m.id AS string) LIKE %:search%)")
    Page<MaterialEntity> findWithSearch(@Param("search") String search, Pageable pageable);

    boolean existsByNameMaterial(String nameMaterial);
}
