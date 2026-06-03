package com.sportshop.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sportshop.converter.CategoryConverter;
import com.sportshop.dto.CategoryDTO;
import com.sportshop.entity.CategoryEntity;
import com.sportshop.repository.CategoryRepository;
import com.sportshop.service.ICategoryService;

@Service
public class CategoryService implements ICategoryService{
	@Autowired
	CategoryRepository categoryRepo;
	@Autowired
	CategoryConverter categoryConverter;
	@Override
	public List<CategoryDTO> getAll(){
		List<CategoryEntity> list =  categoryRepo.findAll();
		List<CategoryDTO> result = new ArrayList<>();
		for(CategoryEntity en: list)
		{
			CategoryDTO dto = categoryConverter.toDTO(en);
			result.add(dto);
		}
		return result;
	}
    @Override
    public CategoryDTO get(Long id) {
        CategoryEntity category = categoryRepo.findById(id).orElse(null);
        CategoryDTO dto = categoryConverter.toDTO(category);
        return dto;
    }
}
