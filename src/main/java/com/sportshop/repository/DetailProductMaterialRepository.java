package com.sportshop.repository;

import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.entity.DetailProductMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DetailProductMaterialRepository
        extends JpaRepository<DetailProductMaterialEntity, DetailProductMaterialId> {
    List<DetailProductMaterialEntity> findByIdIdProduct(Long idProduct);

    @Query("SELECT d FROM DetailProductMaterialEntity d JOIN d.detailMaterial m JOIN d.detailMaterialProduct p " +
            "WHERE (:search IS NULL OR m.nameMaterial LIKE %:search% OR p.name LIKE %:search%)")
    Page<DetailProductMaterialEntity> findWithSearch(@Param("search") String search, Pageable pageable);
}
