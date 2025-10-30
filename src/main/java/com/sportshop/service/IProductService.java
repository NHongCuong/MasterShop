package com.sportshop.service;

import java.util.List;


import com.sportshop.entity.ProductEntity;

public interface IProductService {
	List<ProductEntity> getAllProduct();
	ProductEntity getByID(Long id);
	ProductEntity save(ProductEntity product);
}
