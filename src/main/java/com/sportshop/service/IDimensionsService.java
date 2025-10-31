package com.sportshop.service;

import com.sportshop.dto.DimensionsDTO;

import java.util.List;

public interface IDimensionsService {
    List<DimensionsDTO> getAllDimensionsByProduct(Long idProduct);
    List<DimensionsDTO> getAllDimensions(); // ✅ thêm hàm mới
}
