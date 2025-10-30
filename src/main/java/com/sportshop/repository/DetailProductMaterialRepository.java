package com.sportshop.repository;


import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.entity.DetailProductMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface DetailProductMaterialRepository extends JpaRepository<DetailProductMaterialEntity, DetailProductMaterialId> {
    List<DetailProductMaterialEntity> findByIdIdProduct(Long idProduct);
}
