package com.sportshop.converter;

import org.springframework.stereotype.Component;
import com.sportshop.dto.UserDTO;
import com.sportshop.entity.UserEntity;

@Component
public class UserConverter {
	public UserEntity toEntity(UserDTO dto)
	{
		UserEntity en = new UserEntity();
		en.setAddress(dto.getAddress());
		en.setEmail(dto.getEmail());
		en.setId(dto.getId());
		
		en.setUserStatus(dto.getUserStatus());
		en.setUserType(dto.getUserType());
		
		en.setNameUser(dto.getNameUser());
		en.setPassword(dto.getPassword());
		en.setPhone(dto.getPhone());
		en.setSalt(dto.getSalt());
		en.setVerify(dto.getVerify());
		en.setCreatedAt(dto.getCreatedAt());
		en.setUpdatedAt(dto.getUpdatedAt());
		return en;
	}
	public UserDTO toDTO(UserEntity en)
	{
		UserDTO dto = new UserDTO();
		dto.setAddress(en.getAddress());
		dto.setEmail(en.getEmail());
		dto.setId(en.getId());
		
		dto.setUserStatus(en.getUserStatus());
		dto.setUserType(en.getUserType());
		
		dto.setNameUser(en.getNameUser());
		dto.setPassword(en.getPassword());
		dto.setPhone(en.getPhone());
		dto.setSalt(en.getSalt());
		dto.setVerify(en.getVerify());
		dto.setCreatedAt(en.getCreatedAt());
		dto.setUpdatedAt(en.getUpdatedAt());
		return dto;
	}
}
