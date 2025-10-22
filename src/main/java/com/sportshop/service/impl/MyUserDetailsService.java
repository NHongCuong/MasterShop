package com.sportshop.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sportshop.entity.UserEntity;
import com.sportshop.repository.UserRepository;
import com.sportshop.security.MyUserDetails;


@Service
public class MyUserDetailsService implements UserDetailsService {
	@Autowired
	UserRepository userRepo;
	@Override
	public MyUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<UserEntity> userData = userRepo.findByEmail(email);
		if(userData.isPresent())
			return new MyUserDetails(userData.get().getEmail(), userData.get().getPassword(), userData.get().getUserType().getId());
		throw new UsernameNotFoundException("User not found with username: " + email);
	}

}