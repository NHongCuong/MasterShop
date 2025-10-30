package com.sportshop.service;

import java.util.List;
import java.util.Optional;

import com.sportshop.dto.ProductDTO;
import com.sportshop.entity.ProductEntity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

public interface IProductService {
	List<ProductEntity> getAllProduct();
	ProductEntity getByID(Long id);
	ProductEntity save(ProductEntity product);
}
