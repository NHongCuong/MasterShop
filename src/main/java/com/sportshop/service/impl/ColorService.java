package com.sportshop.service.impl;

import com.sportshop.converter.ColorConverter;
import com.sportshop.dto.ColorDTO;
import com.sportshop.entity.ColorEntity;
import com.sportshop.repository.ColorRepository;
import com.sportshop.service.IColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ColorService implements IColorService {
    @Autowired
    ColorRepository colorRepo;
    @Autowired
    ColorConverter colorConverter;
    @Override
    public List<ColorDTO> getAll() {
        List<ColorEntity> list = colorRepo.findAll();
        List<ColorDTO> result = new ArrayList<>();
        for (ColorEntity en : list) {
            ColorDTO dto = colorConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public ColorDTO get(Long id) {
        ColorEntity category = colorRepo.findOne(id);
        ColorDTO dto = colorConverter.toDTO(category);
        return dto;
    }
}
