package com.sportshop.converter;

import com.sportshop.dto.OrderDTO;
import com.sportshop.entity.OrderEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderConverter {
    public OrderEntity toEntity(OrderDTO dto)
    {
        OrderEntity en = new OrderEntity();
        en.setId(dto.getId());
        en.setShipMethod(dto.getShipMethod());
        en.setMethodofPayment(dto.getMethodofPayment());
        en.setCustomerName(dto.getCustomerName());
        en.setNoteO(dto.getNoteO());
        en.setPhone(dto.getPhone());
        en.setAddressO(dto.getAddressO());


        return en;
    }
    public OrderDTO toDTO(OrderEntity en)
    {
        OrderDTO dto = new OrderDTO();
        dto.setId(en.getId());
        dto.setShipMethod(en.getShipMethod());
        dto.setMethodofPayment(en.getMethodofPayment());
        dto.setCustomerName(en.getCustomerName());
        dto.setNoteO(en.getNoteO());
        dto.setPhone(en.getPhone());
        dto.setAddressO(en.getAddressO());

        return dto;
    }
}
