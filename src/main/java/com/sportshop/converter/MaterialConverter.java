package com.sportshop.converter;


import com.sportshop.dto.MaterialDTO;
import com.sportshop.entity.MaterialEntity;
import org.springframework.stereotype.Component;

@Component
public class MaterialConverter {
    public MaterialEntity toEntity(MaterialDTO dto)
    {
        MaterialEntity en = new MaterialEntity();
        en.setId(dto.getId());
        en.setNameMaterial(dto.getNameMaterial());

        return en;
    }
    public MaterialDTO toDTO(MaterialEntity en)
    {
        MaterialDTO dto = new MaterialDTO();
        dto.setId(en.getId());
        dto.setNameMaterial(en.getNameMaterial());

        return dto;
    }
}
