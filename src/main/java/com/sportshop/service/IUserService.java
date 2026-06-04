package com.sportshop.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sportshop.dto.UserDTO;
import com.sportshop.response.AuthResponse;

public interface IUserService {
	List<UserDTO> getAll();
	Page<UserDTO> findAll(String search, Pageable pageable);
	byte[] exportToExcel() throws Exception;
	UserDTO loadUserByEmail(String email);
	AuthResponse login(UserDTO userDTO);
	AuthResponse signup(UserDTO userDTO);
}
