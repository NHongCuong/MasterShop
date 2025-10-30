package com.sportshop.service;

import com.sportshop.dto.UserTypeDTO;
import java.util.List;

public interface IUserTypeService {
    List<UserTypeDTO> getAll();
    UserTypeDTO get(Long id);

}
