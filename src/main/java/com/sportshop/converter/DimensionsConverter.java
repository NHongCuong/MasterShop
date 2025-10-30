package com.sportshop.converter;

import com.sportshop.dto.DimensionsDTO;
import com.sportshop.entity.DimensionsEntity;
import org.springframework.stereotype.Component;

@Component
public class DimensionsConverter {
    public DimensionsEntity toEntity(DimensionsDTO dto)
    {
        DimensionsEntity en = new DimensionsEntity();
        en.setId(dto.getId());
        en.setNameD(dto.getNameD());
        en.setCreated_at(dto.getCreated_at());
        en.setUpdated_at(dto.getUpdated_at());

        return en;
    }
    public DimensionsDTO toDTO(DimensionsEntity en)
    {
        DimensionsDTO dto = new DimensionsDTO();
        dto.setId(en.getId());
        dto.setNameD(en.getNameD());
        dto.setCreated_at(en.getCreated_at());
        dto.setUpdated_at(en.getUpdated_at());
        return dto;
    }
}
