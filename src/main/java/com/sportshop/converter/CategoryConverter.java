package com.sportshop.converter;

import org.springframework.stereotype.Component;
import com.sportshop.dto.CategoryDTO;
import com.sportshop.entity.CategoryEntity;

@Component
public class CategoryConverter {
	public CategoryEntity toEntity(CategoryDTO dto)
	{
		CategoryEntity en = new CategoryEntity();
		en.setId(dto.getId());
		en.setName(dto.getName());
		en.setIcon(dto.getIcon());
		return en;
	}
	public CategoryDTO toDTO(CategoryEntity en)
	{
		CategoryDTO dto = new CategoryDTO();
		dto.setId(en.getId());
		dto.setName(en.getName());
		dto.setIcon(en.getIcon());
		return dto;
	}
}
