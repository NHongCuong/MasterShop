package com.sportshop.service;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.dto.DetailProductMaterialDTO;
import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.repository.DetailProductColorRepository;
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

        // Giả sử repository có hàm: findByProduct_Id(Long idProduct)
        List<DetailProductMaterialEntity> entities = detailProductMaterialRepository.findByIdIdProduct(idProduct);

        for (DetailProductMaterialEntity entity : entities) {
            String nameMaterial = null;
            if (entity.getDetailMaterial() != null) {
                nameMaterial = entity.getDetailMaterial().getNameMaterial();
            }

            DetailProductMaterialDTO dto = new DetailProductMaterialDTO(
                    entity.getId().getIdMaterial(),
                    entity.getId().getIdProduct(),
                    nameMaterial
            );
            dtos.add(dto);
        }
        return dtos;
    }
    public List<DetailProductMaterialDTO> getAllDetailProductMaterials() {
        List<DetailProductMaterialEntity> entities = detailProductMaterialRepository.findAll();
        List<DetailProductMaterialDTO> dtos = new ArrayList<>();

        for (DetailProductMaterialEntity e : entities) {
            dtos.add(new DetailProductMaterialDTO(
                    e.getId().getIdMaterial(),
                    e.getId().getIdProduct(),
                    e.getDetailMaterial() != null ? e.getDetailMaterial().getNameMaterial() : null
            ));
        }

        return dtos;
    }
}
