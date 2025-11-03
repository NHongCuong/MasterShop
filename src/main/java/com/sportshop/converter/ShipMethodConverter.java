package com.sportshop.converter;

import com.sportshop.dto.ShipMethodDTO;
import com.sportshop.entity.ShipMethodEntity;
import org.springframework.stereotype.Component;

@Component
public class ShipMethodConverter {
    public ShipMethodEntity toEntity(ShipMethodDTO dto)
    {
        ShipMethodEntity en = new ShipMethodEntity();
        en.setId(dto.getId());
        en.setNameSM(dto.getNameSM());

        return en;
    }
    public ShipMethodDTO toDTO(ShipMethodEntity en)
    {
        ShipMethodDTO dto = new ShipMethodDTO();
        dto.setId(en.getId());
        dto.setNameSM(en.getNameSM());
        return dto;
    }
}
