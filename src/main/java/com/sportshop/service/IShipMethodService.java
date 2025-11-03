package com.sportshop.service;

import com.sportshop.dto.ShipMethodDTO;
import com.sportshop.entity.ShipMethodEntity;

import java.util.List;

public interface IShipMethodService {
    List<ShipMethodDTO> getAll();
    ShipMethodDTO get(Long id);
}
