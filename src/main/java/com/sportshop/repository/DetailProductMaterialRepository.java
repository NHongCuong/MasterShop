package com.sportshop.repository;

import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.entity.DetailProductMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DetailProductMaterialRepository extends JpaRepository<DetailProductMaterialEntity, DetailProductMaterialId> {
    List<DetailProductMaterialEntity> findByIdIdProduct(Long idProduct);
}
