package com.sportshop.converter;

import com.sportshop.dto.ColorDTO;
import com.sportshop.entity.ColorEntity;
import org.springframework.stereotype.Component;

@Component
public class ColorConverter {

    public ColorDTO toDTO(ColorEntity entity) {
        if (entity == null) return null;
        ColorDTO dto = new ColorDTO();
        dto.setId(entity.getId());
        dto.setNameColor(entity.getNameColor());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public ColorEntity toEntity(ColorDTO dto) {
        if (dto == null) return null;
        ColorEntity entity = new ColorEntity();
        entity.setId(dto.getId());
        entity.setNameColor(dto.getNameColor());
        return entity;
    }
}
