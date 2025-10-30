package com.sportshop.service.impl;

import com.sportshop.converter.MaterialConverter;
import com.sportshop.dto.MaterialDTO;
import com.sportshop.entity.MaterialEntity;
import com.sportshop.repository.MaterialRepository;
import com.sportshop.service.IMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialService implements IMaterialService {
    @Autowired
    MaterialRepository materialRepo;
    @Autowired
    MaterialConverter materialConverter;
    @Override
    public List<MaterialDTO> getAll() {
        List<MaterialEntity> list = materialRepo.findAll();
        List<MaterialDTO> result = new ArrayList<>();
        for (MaterialEntity en : list) {
            MaterialDTO dto = materialConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public MaterialDTO getById(Long id) {
        MaterialEntity category = materialRepo.findOne(id);
        MaterialDTO dto = materialConverter.toDTO(category);
        return dto;
    }
}
