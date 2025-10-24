package com.sportshop.converter;

import com.sportshop.dto.BillStatusDTO;
import com.sportshop.entity.BillStatusEntity;
import org.springframework.stereotype.Component;

@Component
public class BillStatusConverter {
    public BillStatusEntity toEntity(BillStatusDTO dto)
    {
        BillStatusEntity en = new BillStatusEntity();
        en.setId(dto.getId());
        en.setNameBS(dto.getNameBS());
        return en;
    }
    public BillStatusDTO toDTO(BillStatusEntity en)
    {
        BillStatusDTO dto = new BillStatusDTO();
        dto.setId(en.getId());
        dto.setNameBS(en.getNameBS());
        return dto;
    }
}
