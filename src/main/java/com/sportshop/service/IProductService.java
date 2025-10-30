package com.sportshop.service;

import java.util.List;
<<<<<<< HEAD
import java.util.Optional;

=======
>>>>>>> f1710986fd81cb55e3a0509a9c18bd566e888d8a
import com.sportshop.dto.ProductDTO;
import com.sportshop.entity.ProductEntity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

public interface IProductService {
	List<ProductEntity> getAllProduct();
	ProductEntity getByID(Long id);
	ProductEntity save(ProductEntity product);
}
