package com.sportshop.service.impl;


import com.sportshop.converter.DimensionsConverter;
import com.sportshop.dto.CategoryDTO;
import com.sportshop.dto.DimensionsDTO;
import com.sportshop.entity.CategoryEntity;
import com.sportshop.entity.DimensionsEntity;
import com.sportshop.repository.DimensionsRepository;
import com.sportshop.service.IDimensionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DimensionsService implements IDimensionsService {
    @Autowired
    DimensionsRepository dimensionsRepo;
    @Autowired
    DimensionsConverter dimensionsConverter;
    @Override
    public List<DimensionsDTO> getAll() {
        List<DimensionsEntity> list = dimensionsRepo.findAll();
        List<DimensionsDTO> result = new ArrayList<>();
        for (DimensionsEntity en : list) {
            DimensionsDTO dto = dimensionsConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public DimensionsDTO get(Long id) {
        DimensionsEntity category = dimensionsRepo.findOne(id);
        DimensionsDTO dto = dimensionsConverter.toDTO(category);
        return dto;
    }
}
