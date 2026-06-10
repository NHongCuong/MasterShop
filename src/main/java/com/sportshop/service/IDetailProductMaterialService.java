package com.sportshop.service;

import com.sportshop.dto.DetailProductMaterialDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IDetailProductMaterialService {
    List<DetailProductMaterialDTO> findAllDTO();
    DetailProductMaterialDTO get(Long idMaterial, Long idProduct);
    Page<DetailProductMaterialDTO> findAll(String search, Pageable pageable);
    byte[] exportToExcel() throws Exception;
    void delete(Long idMaterial, Long idProduct);
    void save(DetailProductMaterialDTO dto);
    void update(Long oldIdMaterial, Long oldIdProduct, DetailProductMaterialDTO dto);
    void importExcel(org.springframework.web.multipart.MultipartFile file) throws Exception;
}
