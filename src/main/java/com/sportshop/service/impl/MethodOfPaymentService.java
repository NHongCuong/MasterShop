package com.sportshop.service.impl;

import com.sportshop.converter.MethodOfPaymentConverter;
import com.sportshop.converter.ShipMethodConverter;
import com.sportshop.dto.MethodOfPaymentDTO;
import com.sportshop.dto.ShipMethodDTO;
import com.sportshop.entity.MethodOfPaymentEntity;
import com.sportshop.entity.ShipMethodEntity;
import com.sportshop.repository.MethodOfPaymentRepository;
import com.sportshop.repository.ShipMethodRepository;
import com.sportshop.service.IMethodOfPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MethodOfPaymentService implements IMethodOfPaymentService {
    @Autowired
    MethodOfPaymentRepository methodOfPaymentRepository;
    @Autowired
    MethodOfPaymentConverter methodOfPaymentConverter;
    @Override
    public List<MethodOfPaymentDTO> getAll() {
        List<MethodOfPaymentEntity> list = methodOfPaymentRepository.findAll();
        List<MethodOfPaymentDTO> result = new ArrayList<>();
        for (MethodOfPaymentEntity en : list) {
            MethodOfPaymentDTO dto = methodOfPaymentConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public MethodOfPaymentDTO get(Long id) {
        MethodOfPaymentEntity methodOfPayment = methodOfPaymentRepository.findOne(id);
        MethodOfPaymentDTO dto = methodOfPaymentConverter.toDTO(methodOfPayment);
        return dto;
    }
}
