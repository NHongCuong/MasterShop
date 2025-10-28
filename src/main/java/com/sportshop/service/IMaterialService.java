package com.sportshop.service;

import com.sportshop.dto.MaterialDTO;

import java.util.List;

public interface IMaterialService {
    List<MaterialDTO> getAll();
    MaterialDTO getById(Long id);

}
