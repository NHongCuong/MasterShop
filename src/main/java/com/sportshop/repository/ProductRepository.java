package com.sportshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sportshop.entity.ProductEntity;

public interface ProductRepository  extends JpaRepository<ProductEntity, Long>{
    //List<ProductEntity> id(Long id);
    //Optional<ProductEntity> findById(Long id);
}
