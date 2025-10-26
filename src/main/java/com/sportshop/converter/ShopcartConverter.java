package com.sportshop.converter;

import com.sportshop.dto.ShopcartDTO;
import com.sportshop.entity.ShopcartEntity;
import org.springframework.stereotype.Component;

@Component
public class ShopcartConverter {
    public ShopcartEntity toEntity(ShopcartDTO dto)
    {
        ShopcartEntity en = new ShopcartEntity();
        en.setId(dto.getId());
        en.setCartStatus(dto.getCartStatus());
        en.setUserSC(dto.getUserSC());
        return en;
    }
    public ShopcartDTO toDTO(ShopcartEntity en)
    {
        ShopcartDTO dto = new ShopcartDTO();
        dto.setId(en.getId());
        dto.setCartStatus(en.getCartStatus());
        dto.setUserSC(en.getUserSC());
        return dto;
    }
}
