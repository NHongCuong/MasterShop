package com.sportshop.service;

import com.sportshop.dto.CartStatusDTO;

import java.util.List;

public interface ICartStatusService {
    List<CartStatusDTO> getAll();
    CartStatusDTO get(Long id);
}
