package com.sportshop.service;

import com.sportshop.dto.BillDTO;
import com.sportshop.dto.CartDetailDTO;
import com.sportshop.entity.CartDetailId;

import java.util.List;
import java.util.Optional;

public interface ICartDetailService {
    List<CartDetailDTO> findAllDTO();
    CartDetailDTO get(CartDetailId id);
}