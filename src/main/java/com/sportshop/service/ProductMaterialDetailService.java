package com.sportshop.service;

import com.sportshop.dto.DetailProductMaterialDTO;
import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.repository.DetailProductMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductMaterialDetailService {
    private final DetailProductMaterialRepository detailProductMaterialRepository;

    @Autowired
    public ProductMaterialDetailService(DetailProductMaterialRepository detailProductMaterialRepository) {
        this.detailProductMaterialRepository = detailProductMaterialRepository;
    }

    /**
     * Lấy toàn bộ chi tiết màu theo idProduct
     */
    public List<DetailProductMaterialDTO> getAllDetailProductMaterialsByProduct(Long idProduct) {
        List<DetailProductMaterialDTO> dtos = new ArrayList<>();
        List<DetailProductMaterialEntity> entities = detailProductMaterialRepository.findByIdIdProduct(idProduct);

        for (DetailProductMaterialEntity entity : entities) {
            DetailProductMaterialDTO dto = new DetailProductMaterialDTO();
            dto.setIdMaterial(entity.getId().getIdMaterial());
            dto.setIdProduct(entity.getId().getIdProduct());
            dto.setNameMaterial(entity.getDetailMaterial() != null ? entity.getDetailMaterial().getNameMaterial() : null);
            dto.setProductName(entity.getDetailMaterialProduct() != null ? entity.getDetailMaterialProduct().getName() : null);
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<DetailProductMaterialDTO> getAllDetailProductMaterials() {
        List<DetailProductMaterialEntity> entities = detailProductMaterialRepository.findAll();
        List<DetailProductMaterialDTO> dtos = new ArrayList<>();

        for (DetailProductMaterialEntity e : entities) {
            DetailProductMaterialDTO dto = new DetailProductMaterialDTO();
            dto.setIdMaterial(e.getId().getIdMaterial());
            dto.setIdProduct(e.getId().getIdProduct());
            dto.setNameMaterial(e.getDetailMaterial() != null ? e.getDetailMaterial().getNameMaterial() : null);
            dto.setProductName(e.getDetailMaterialProduct() != null ? e.getDetailMaterialProduct().getName() : null);
            dto.setCreatedAt(e.getCreatedAt());
            dto.setUpdatedAt(e.getUpdatedAt());
            dtos.add(dto);
        }

        return dtos;
    }
}
