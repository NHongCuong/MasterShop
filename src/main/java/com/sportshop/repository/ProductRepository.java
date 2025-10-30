package com.sportshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sportshop.entity.ProductEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ProductRepository  extends JpaRepository<ProductEntity, Long>{

}
