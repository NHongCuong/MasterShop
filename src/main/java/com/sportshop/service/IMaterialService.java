package com.sportshop.service;

import com.sportshop.dto.MaterialDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IMaterialService {
    List<MaterialDTO> findAllDTO();
    Page<MaterialDTO> findAll(String search, Pageable pageable);
    byte[] exportToExcel() throws Exception;
    void delete(Long id);
    void save(MaterialDTO dto);
    void update(Long id, MaterialDTO dto);
}
