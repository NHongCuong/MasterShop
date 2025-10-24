package com.sportshop.converter;

import com.sportshop.dto.BillStatusHistoryDTO;
import com.sportshop.entity.BillStatusHistoryEntity;

public class BillStatusHistoryConvert {
    public BillStatusHistoryEntity toEntity(BillStatusHistoryDTO dto)
    {
        BillStatusHistoryEntity en = new BillStatusHistoryEntity();
        en.setId(dto.getId());
        en.setBill(dto.getBill());
        en.setBillStatus(dto.getBillStatus());
        en.setUserbillSH(dto.getUserbillSH());
        en.setDate_BSH(dto.getDate_BSH());

        return en;
    }
    public BillStatusHistoryDTO toDTO(BillStatusHistoryEntity en)
    {
        BillStatusHistoryDTO dto = new BillStatusHistoryDTO();
        dto.setId(en.getId());
        dto.setBillStatus(en.getBillStatus());
        dto.setBill(en.getBill());
        dto.setUserbillSH(en.getUserbillSH());
        dto.setDate_BSH(en.getDate_BSH());

        return dto;
    }
}
