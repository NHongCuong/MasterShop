package com.sportshop.service;

import com.sportshop.dto.DimensionsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IDimensionsService {
    List<DimensionsDTO> findAllDTO();
    List<DimensionsDTO> findByProductId(Long productId);
    DimensionsDTO get(Long id);
    Page<DimensionsDTO> findAll(String search, Pageable pageable);
    byte[] exportToExcel() throws Exception;
    void delete(Long id);
    void save(DimensionsDTO dto);
    void update(Long id, DimensionsDTO dto);
}
