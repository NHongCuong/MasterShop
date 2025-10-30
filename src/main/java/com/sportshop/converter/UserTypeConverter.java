package com.sportshop.converter;

import com.sportshop.dto.UserTypeDTO;
import com.sportshop.entity.UserTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class UserTypeConverter {
	public UserTypeEntity toEntity(UserTypeDTO dto)
	{
		UserTypeEntity en = new UserTypeEntity();
		en.setId(dto.getId());
		en.setName(dto.getName());

		return en;
	}
	public UserTypeDTO toDTO(UserTypeEntity en)
	{
        UserTypeDTO dto = new UserTypeDTO();
		dto.setId(en.getId());
		dto.setName(en.getName());
		return dto;
	}
}
