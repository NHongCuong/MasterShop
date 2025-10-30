package com.sportshop.converter;

import com.sportshop.dto.ColorDTO;
import com.sportshop.entity.ColorEntity;
import org.springframework.stereotype.Component;

@Component
public class ColorConverter {
    public ColorEntity toEntity(ColorDTO dto)
    {
        ColorEntity en = new ColorEntity();
        en.setId(dto.getId());
        en.setNameColor(dto.getNameColor());

        return en;
    }
    public ColorDTO toDTO(ColorEntity en)
    {
        ColorDTO dto = new ColorDTO();
        dto.setId(en.getId());
        dto.setNameColor(en.getNameColor());
        return dto;
    }
}
