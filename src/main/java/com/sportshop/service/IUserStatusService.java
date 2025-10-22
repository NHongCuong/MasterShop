package com.sportshop.service;

import com.sportshop.dto.UserStatusDTO;

import java.util.List;

public interface IUserStatusService {
    List<UserStatusDTO> getAll();
    UserStatusDTO get(Long id);

}
