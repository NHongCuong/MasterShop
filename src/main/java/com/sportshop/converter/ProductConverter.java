package com.sportshop.converter;

import org.springframework.stereotype.Component;
import com.sportshop.dto.ProductDTO;
import com.sportshop.entity.ProductEntity;

@Component
public class ProductConverter {
	public ProductEntity toEntity(ProductDTO dto)
	{
		ProductEntity en = new ProductEntity();
		en.setAmount(dto.getAmount());
		en.setAvatar(dto.getAvatar());
		en.setCategory(dto.getCategory());
		en.setDescription(dto.getDescription());
		en.setId(dto.getId());
		en.setName(dto.getName());
		en.setPrice(dto.getPrice());
		en.setSupplier(dto.getSupplier());
		
		return en;
	}
	public ProductDTO toDTO(ProductEntity en)
	{
		ProductDTO dto = new ProductDTO();
		dto.setAmount(en.getAmount());
		dto.setAvatar(en.getAvatar());
		dto.setCategory(en.getCategory());
		dto.setDescription(en.getDescription());
		dto.setId(en.getId());
		dto.setName(en.getName());
		dto.setPrice(en.getPrice());
		dto.setSupplier(en.getSupplier());
		return dto;
	}
}
