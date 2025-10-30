package com.sportshop.service;

import com.sportshop.dto.ColorDTO;
import java.util.List;

public interface IColorService {
    List<ColorDTO> getAll();
    ColorDTO get(Long id);
}
