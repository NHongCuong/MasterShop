package com.sportshop.converter;

import com.sportshop.dto.DimensionsDTO;
import com.sportshop.entity.DimensionsEntity;
import com.sportshop.entity.ProductEntity;
import com.sportshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DimensionsConverter {

    @Autowired
    private ProductRepository productRepository;

    public DimensionsDTO toDTO(DimensionsEntity entity) {
        if (entity == null) return null;
        DimensionsDTO dto = new DimensionsDTO();
        dto.setId(entity.getId());
        dto.setNameD(entity.getNameD());
        dto.setCreatedAt(entity.getCreated_at());
        dto.setUpdatedAt(entity.getUpdated_at());
        if (entity.getDemensions() != null) {
            dto.setProductId(entity.getDemensions().getId());
            dto.setProductName(entity.getDemensions().getName());
        }
        return dto;
    }

    public DimensionsEntity toEntity(DimensionsDTO dto) {
        if (dto == null) return null;
        DimensionsEntity entity = new DimensionsEntity();
        entity.setId(dto.getId());
        entity.setNameD(dto.getNameD());
        if (dto.getProductId() != null) {
            ProductEntity product = productRepository.findById(dto.getProductId()).orElse(null);
            entity.setDemensions(product);
        }
        return entity;
    }
}
