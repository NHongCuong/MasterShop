package com.sportshop.service.impl;

import com.sportshop.converter.UserTypeConverter;
import com.sportshop.dto.UserTypeDTO;
import com.sportshop.entity.UserTypeEntity;
import com.sportshop.repository.UserTypeRepository;
import com.sportshop.service.IUserTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserTypeService implements IUserTypeService {
    @Autowired
    UserTypeRepository usertypeRepo;
    @Autowired
    UserTypeConverter usertypeConverter;
    @Override
    public List<UserTypeDTO> getAll() {
        List<UserTypeEntity> list = usertypeRepo.findAll();
        List<UserTypeDTO> result = new ArrayList<>();
        for (UserTypeEntity en : list) {
            UserTypeDTO dto = usertypeConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }

    @Override
    public UserTypeDTO get(Long id) {
        return null;
    }
}
