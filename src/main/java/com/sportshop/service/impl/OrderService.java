package com.sportshop.service.impl;

import com.sportshop.converter.OrderConverter;
import com.sportshop.dto.OrderDTO;
import com.sportshop.entity.ColorEntity;
import com.sportshop.entity.OrderEntity;
import com.sportshop.repository.OrderRepository;
import com.sportshop.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService implements IOrderService {
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderConverter orderConverter;
    @Override
    public List<OrderDTO> getAll() {
        List<OrderEntity> list = orderRepository.findAll();
        List<OrderDTO> result = new ArrayList<>();
        for (OrderEntity en : list) {
            OrderDTO dto = orderConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public OrderDTO get(Long id) {
        OrderEntity order = orderRepository.findOne(id);
        OrderDTO dto = orderConverter.toDTO(order);
        return dto;
    };

}
