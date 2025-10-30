package com.sportshop.service.impl;

import com.sportshop.converter.CartStatusConverter;
import com.sportshop.dto.CartStatusDTO;
import com.sportshop.entity.CartStatusEntity;
import com.sportshop.repository.CartStatusRepository;
import com.sportshop.service.ICartStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartStatusService  implements ICartStatusService {
    @Autowired
    CartStatusRepository cartstatusRepo;
    @Autowired
    CartStatusConverter cartstatusConverter;
    @Override
    public List<CartStatusDTO> getAll() {
        List<CartStatusEntity> list = cartstatusRepo.findAll();
        List<CartStatusDTO> result = new ArrayList<>();
        for (CartStatusEntity en : list) {
            CartStatusDTO dto = cartstatusConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }

    @Override
    public CartStatusDTO get(Long id) {
        return null;
    }
}
