package com.sportshop.service.impl;

import com.sportshop.converter.ColorConverter;
import com.sportshop.converter.ShipMethodConverter;
import com.sportshop.dto.ColorDTO;
import com.sportshop.dto.ShipMethodDTO;
import com.sportshop.entity.ColorEntity;
import com.sportshop.entity.ShipMethodEntity;
import com.sportshop.repository.ColorRepository;
import com.sportshop.repository.ShipMethodRepository;
import com.sportshop.service.IShipMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShipMethodService implements IShipMethodService {
    @Autowired
    ShipMethodRepository shipMethodRepository;
    @Autowired
    ShipMethodConverter shopMethodConverter;
    @Override
    public List<ShipMethodDTO> getAll() {
        List<ShipMethodEntity> list = shipMethodRepository.findAll();
        List<ShipMethodDTO> result = new ArrayList<>();
        for (ShipMethodEntity en : list) {
            ShipMethodDTO dto = shopMethodConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public ShipMethodDTO get(Long id) {
        ShipMethodEntity shopmethod = shipMethodRepository.findOne(id);
        ShipMethodDTO dto = shopMethodConverter.toDTO(shopmethod);
        return dto;
    }
}
