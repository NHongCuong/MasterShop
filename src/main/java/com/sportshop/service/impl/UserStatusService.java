package com.sportshop.service.impl;

import com.sportshop.converter.UserStatusConverter;
import com.sportshop.dto.UserStatusDTO;
import com.sportshop.entity.UserStatusEntity;
import com.sportshop.repository.UserStatusRepository;
import com.sportshop.service.IUserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserStatusService implements IUserStatusService {
    @Autowired
    UserStatusRepository userstatusRepo;
    @Autowired
    UserStatusConverter userstatusConverter;
    @Override
    public List<UserStatusDTO> getAll() {
        List<UserStatusEntity> list = userstatusRepo.findAll();
        List<UserStatusDTO> result = new ArrayList<>();
        for (UserStatusEntity en : list) {
            UserStatusDTO dto = userstatusConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }

    @Override
    public UserStatusDTO get(Long id) {
        return null;
    }
}
