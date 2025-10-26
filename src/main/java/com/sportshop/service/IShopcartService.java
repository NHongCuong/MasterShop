package com.sportshop.service;

import com.sportshop.dto.ShopcartDTO;

import java.util.List;

public interface IShopcartService {
    List<ShopcartDTO> getAll();
    ShopcartDTO get(Long id);
}
