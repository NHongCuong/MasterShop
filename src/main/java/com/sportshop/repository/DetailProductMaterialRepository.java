package com.sportshop.repository;

import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.entity.DetailProductMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
=======
import java.util.List;

>>>>>>> f1710986fd81cb55e3a0509a9c18bd566e888d8a
public interface DetailProductMaterialRepository extends JpaRepository<DetailProductMaterialEntity, DetailProductMaterialId> {
    List<DetailProductMaterialEntity> findByIdIdProduct(Long idProduct);
}
