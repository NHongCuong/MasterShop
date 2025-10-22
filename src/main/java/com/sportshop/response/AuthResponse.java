package com.sportshop.response;

import com.sportshop.dto.UserDTO;

public class AuthResponse extends BaseResponse{
	public UserDTO user;
	public AuthResponse(Integer status, String message,UserDTO user, String token) {
		super();
		this.user = user;
		this.status = status;
		this.message = message;
		this.token = token;
	}
	public AuthResponse(Integer status, String message,UserDTO user) {
		super();
		this.user = user;
		this.status = status;
		this.message = message;
	}
	public AuthResponse(Integer status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	
}
