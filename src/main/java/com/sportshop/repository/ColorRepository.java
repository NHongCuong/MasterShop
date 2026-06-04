package com.sportshop.repository;

import com.sportshop.entity.ColorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ColorRepository extends JpaRepository<ColorEntity,Long> {
    @Query("SELECT c FROM ColorEntity c " +
           "WHERE (:search IS NULL OR c.nameColor LIKE %:search% OR CAST(c.id AS string) LIKE %:search%)")
    Page<ColorEntity> findWithSearch(@Param("search") String search, Pageable pageable);

    boolean existsByNameColor(String nameColor);
}
