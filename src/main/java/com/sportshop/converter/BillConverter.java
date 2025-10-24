package com.sportshop.converter;

import com.sportshop.dto.BillDTO;
import com.sportshop.entity.BillEntity;
import org.springframework.stereotype.Component;

@Component
public class BillConverter {
    public BillEntity toEntity(BillDTO dto)
    {
        BillEntity en = new BillEntity();
        en.setBill(dto.getBill());
        en.setOrderbill(dto.getOrderbill());
        en.setId(dto.getId());

        en.setVatRate(dto.getVatRate());
        en.setVatAmount(dto.getVatAmount());

        en.setTotalMoney(dto.getTotalMoney());
        en.setTotalMoneyCheckout(dto.getTotalMoneyCheckout());
        en.setTotalMoneyaftersaleoff(dto.getTotalMoneyaftersaleoff());
        en.setCreateDate(dto.getCreateDate());

        return en;
    }
    public BillDTO toDTO(BillEntity en)
    {
        BillDTO dto = new BillDTO();
        dto.setBill(en.getBill());
        dto.setOrderbill(en.getOrderbill());
        dto.setId(en.getId());

        dto.setVatRate(en.getVatRate());
        dto.setVatAmount(en.getVatAmount());

        dto.setTotalMoney(en.getTotalMoney());
        dto.setTotalMoneyCheckout(en.getTotalMoneyCheckout());
        dto.setTotalMoneyaftersaleoff(en.getTotalMoneyaftersaleoff());
        dto.setCreateDate(en.getCreateDate());

        return dto;
    }
}
