package com.sportshop.repository;

import com.sportshop.entity.OderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OderRepository extends JpaRepository<OderEntity, Long> {

    @Query("SELECT o FROM OderEntity o WHERE " +
           "(:search IS NULL OR o.customerName LIKE %:search% " +
           "OR o.phone LIKE %:search% " +
           "OR CAST(o.id AS string) LIKE %:search%)")
    Page<OderEntity> findWithSearch(@Param("search") String search, Pageable pageable);
}
