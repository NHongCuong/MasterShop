package com.sportshop.service;

import com.sportshop.dto.CartDetailDTO;
//import com.sportshop.entity.CartDetailId;
import java.util.List;

public interface ICartDetailService {
    List<CartDetailDTO> findAllDTO();
    CartDetailDTO get(Long id);
    void save(com.sportshop.entity.CartDetailEntity entity);
    List<CartDetailDTO> findActiveItemsByUserId(Long userId);
}
