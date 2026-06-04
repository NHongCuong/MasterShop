package com.sportshop.repository;

import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.DetailProductColorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface DetailProductColorRepository extends JpaRepository<DetailProductColorEntity, DetailProductColorId> {
    List<DetailProductColorEntity> findByIdIdProduct(Long idProduct);

    @Query("SELECT d FROM DetailProductColorEntity d JOIN d.detailColor c JOIN d.detailColorProduct p " +
           "WHERE (:search IS NULL OR c.nameColor LIKE %:search% OR p.name LIKE %:search%)")
    Page<DetailProductColorEntity> findWithSearch(@Param("search") String search, Pageable pageable);
}
