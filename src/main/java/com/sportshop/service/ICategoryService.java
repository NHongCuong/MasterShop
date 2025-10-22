package com.sportshop.service;

import java.util.List;

import com.sportshop.dto.CategoryDTO;
import com.sportshop.dto.ProductDTO;

public interface ICategoryService {
	List<CategoryDTO> getAll();
    CategoryDTO get(Long id);
}
