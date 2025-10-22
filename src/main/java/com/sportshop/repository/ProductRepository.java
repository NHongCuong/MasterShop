package com.sportshop.repository;

import com.sportshop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sportshop.entity.ProductEntity;
import com.sportshop.dto.ProductDTO;
import java.util.List;
import java.util.Optional;

public interface ProductRepository  extends JpaRepository<ProductEntity, Long>{
    //List<ProductEntity> id(Long id);
    //Optional<ProductEntity> findById(Long id);
}
