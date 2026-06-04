package com.sportshop.service;

import com.sportshop.dto.DetailProductColorDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IDetailProductColorService {
    List<DetailProductColorDTO> findAllDTO();
    DetailProductColorDTO get(Long idColor, Long idProduct);
    Page<DetailProductColorDTO> findAll(String search, Pageable pageable);
    byte[] exportToExcel() throws Exception;
    void delete(Long idColor, Long idProduct);
    void save(DetailProductColorDTO dto);
    void update(Long oldIdColor, Long oldIdProduct, DetailProductColorDTO dto);
}
