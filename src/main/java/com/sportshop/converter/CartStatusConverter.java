package com.sportshop.converter;

import com.sportshop.dto.CartStatusDTO;
import com.sportshop.entity.CartStatusEntity;
import org.springframework.stereotype.Component;

@Component
public class CartStatusConverter {
    public CartStatusEntity toEntity(CartStatusDTO dto)
    {
        CartStatusEntity en = new CartStatusEntity();
        en.setId(dto.getId());
        en.setNameCS(dto.getNameCS());
        en.setCreated_at(dto.getCreated_at());
        en.setUpdated_at(dto.getUpdated_at());

        return en;
    }
    public CartStatusDTO toDTO(CartStatusEntity en)
    {
        CartStatusDTO dto = new CartStatusDTO();
        dto.setId(en.getId());
        dto.setNameCS(en.getNameCS());
        dto.setCreated_at(en.getCreated_at());
        dto.setUpdated_at(en.getUpdated_at());
        return dto;
    }
}
