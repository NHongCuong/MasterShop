package com.sportshop.service;

import java.util.List;
import com.sportshop.dto.ProductDTO;

public interface IProductService {
	List<ProductDTO> getAll();
	ProductDTO get(Long id);
}
