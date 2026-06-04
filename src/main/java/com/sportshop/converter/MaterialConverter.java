package com.sportshop.converter;

import com.sportshop.dto.MaterialDTO;
import com.sportshop.entity.MaterialEntity;
import org.springframework.stereotype.Component;

@Component
public class MaterialConverter {

    public MaterialDTO toDTO(MaterialEntity entity) {
        if (entity == null) return null;
        MaterialDTO dto = new MaterialDTO();
        dto.setId(entity.getId());
        dto.setNameMaterial(entity.getNameMaterial());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public MaterialEntity toEntity(MaterialDTO dto) {
        if (dto == null) return null;
        MaterialEntity entity = new MaterialEntity();
        entity.setId(dto.getId());
        entity.setNameMaterial(dto.getNameMaterial());
        return entity;
    }
}
