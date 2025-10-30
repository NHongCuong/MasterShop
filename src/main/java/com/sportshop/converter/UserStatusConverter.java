package com.sportshop.converter;

import com.sportshop.dto.UserStatusDTO;
import com.sportshop.entity.UserStatusEntity;
import org.springframework.stereotype.Component;

@Component
public class UserStatusConverter {
	public UserStatusEntity toEntity(UserStatusDTO dto)
	{
        UserStatusEntity en = new UserStatusEntity();
		en.setId(dto.getId());
		en.setName(dto.getName());

		return en;
	}
	public UserStatusDTO toDTO(UserStatusEntity en)
	{
        UserStatusDTO dto = new UserStatusDTO();
		dto.setId(en.getId());
		dto.setName(en.getName());
		return dto;
	}
}
