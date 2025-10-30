package com.sportshop.service;

import com.sportshop.dto.DimensionsDTO;
import java.util.List;

public interface IDimensionsService {
    List<DimensionsDTO> getAll();
    DimensionsDTO get(Long id);

}
