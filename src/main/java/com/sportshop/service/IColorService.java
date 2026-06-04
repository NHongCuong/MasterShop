package com.sportshop.service;

import com.sportshop.dto.ColorDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IColorService {
    List<ColorDTO> findAllDTO();
    Page<ColorDTO> findAll(String search, Pageable pageable);
    byte[] exportToExcel() throws Exception;
    void delete(Long id);
    void save(ColorDTO dto);
    void update(Long id, ColorDTO dto);
}
