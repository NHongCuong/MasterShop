package com.sportshop.repository;

import com.sportshop.entity.DimensionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DimensionsRepository extends JpaRepository<DimensionsEntity, Long> {
    java.util.List<DimensionsEntity> findByDemensions_Id(Long productId);

    @Query("SELECT d FROM DimensionsEntity d JOIN d.demensions p " +
           "WHERE (:search IS NULL OR d.nameD LIKE %:search% OR p.name LIKE %:search%)")
    Page<DimensionsEntity> findWithSearch(@Param("search") String search, Pageable pageable);

    boolean existsByNameDAndDemensions_Id(String nameD, Long productId);
}
