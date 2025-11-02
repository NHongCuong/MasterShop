package com.sportshop.service;

import com.sportshop.dto.OrderDTO;

import java.util.List;

public interface IOrderService {
    List<OrderDTO> getAll();
    OrderDTO get(Long id);
}
