package com.sportshop.service;

import com.sportshop.dto.MethodOfPaymentDTO;

import java.util.List;

public interface IMethodOfPaymentService {
    List<MethodOfPaymentDTO> getAll();
    MethodOfPaymentDTO get(Long id);
}
