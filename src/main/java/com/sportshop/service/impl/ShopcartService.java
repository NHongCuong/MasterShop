package com.sportshop.service.impl;

import com.sportshop.converter.BillConverter;
import com.sportshop.converter.ShopcartConverter;
import com.sportshop.dto.BillDTO;
import com.sportshop.dto.ShopcartDTO;
import com.sportshop.entity.BillEntity;
import com.sportshop.entity.ShopcartEntity;
import com.sportshop.repository.BillRepository;
import com.sportshop.repository.ShopcartRepository;
import com.sportshop.service.IShopcartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShopcartService implements IShopcartService {
    @Autowired
    ShopcartRepository shopcartRepo;
    @Autowired
    ShopcartConverter shopcartConverter;
    @Override
    public List<ShopcartDTO> getAll(){
        List<ShopcartEntity> list =  shopcartRepo.findAll();
        List<ShopcartDTO> result = new ArrayList<>();
        for(ShopcartEntity en: list)
        {
            ShopcartDTO dto = shopcartConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public ShopcartDTO get(Long id) {
        ShopcartEntity shopcart = shopcartRepo.findOne(id);
        ShopcartDTO dto = shopcartConverter.toDTO(shopcart);
        return dto;
    }
}
