package com.sportshop.converter;

import com.sportshop.dto.MethodOfPaymentDTO;
import com.sportshop.entity.MethodOfPaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class MethodOfPaymentConverter {
    public MethodOfPaymentEntity toEntity(MethodOfPaymentDTO dto)
    {
        MethodOfPaymentEntity en = new MethodOfPaymentEntity();
        en.setId(dto.getId());
        en.setName_mop(dto.getName_mop());

        return en;
    }
    public MethodOfPaymentDTO toDTO(MethodOfPaymentEntity en)
    {
        MethodOfPaymentDTO dto = new MethodOfPaymentDTO();
        dto.setId(en.getId());
        dto.setName_mop(en.getName_mop());
        return dto;
    }
}
