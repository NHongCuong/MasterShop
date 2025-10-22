package com.sportshop.converter;


import com.sportshop.dto.SupplierDTO;
import com.sportshop.entity.SupplierEntity;
import org.springframework.stereotype.Component;

@Component
public class SupplierConverter {
	public SupplierEntity toEntity(SupplierDTO dto)
	{
        SupplierEntity en = new SupplierEntity();
		en.setId(dto.getId());
		en.setName(dto.getName());
		en.setAddress(dto.getAddress());
        en.setPhone(dto.getPhone());
        en.setEmail(dto.getEmail());
        en.setWebsite(dto.getWebsite());
		return en;
	}
	public SupplierDTO toDTO(SupplierEntity en)
	{
        SupplierDTO dto = new SupplierDTO();
        dto.setId(en.getId());
        dto.setName(en.getName());
        dto.setAddress(en.getAddress());
        dto.setPhone(en.getPhone());
        dto.setEmail(en.getEmail());
        dto.setWebsite(en.getWebsite());
        return dto;
	}
}
