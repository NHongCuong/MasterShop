package com.sportshop.converter;

import com.sportshop.dto.DetailProductMaterialDTO;
import com.sportshop.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DetailProductMaterialConverter {
    public DetailProductMaterialEntity toEntity(DetailProductMaterialDTO dto) {
        if (dto == null) return null;
        DetailProductMaterialEntity entity = new DetailProductMaterialEntity();
        
        DetailProductMaterialId id = new DetailProductMaterialId(dto.getIdMaterial(), dto.getIdProduct());
        entity.setId(id);

        if (dto.getIdMaterial() != null) {
            MaterialEntity m = new MaterialEntity();
            m.setId(dto.getIdMaterial());
            m.setNameMaterial(dto.getNameMaterial());
            entity.setDetailMaterial(m);
        }

        if (dto.getIdProduct() != null) {
            ProductEntity p = new ProductEntity();
            p.setId(dto.getIdProduct());
            p.setName(dto.getProductName());
            entity.setDetailMaterialProduct(p);
        }
        
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    public DetailProductMaterialDTO toDTO(DetailProductMaterialEntity entity) {
        if (entity == null) return null;
        DetailProductMaterialDTO dto = new DetailProductMaterialDTO();
        
        if (entity.getId() != null) {
            dto.setIdMaterial(entity.getId().getIdMaterial());
            dto.setIdProduct(entity.getId().getIdProduct());
        }
        
        if (entity.getDetailMaterialProduct() != null) {
            dto.setProductName(entity.getDetailMaterialProduct().getName());
        }
        
        if (entity.getDetailMaterial() != null) {
            dto.setNameMaterial(entity.getDetailMaterial().getNameMaterial());
        }
        
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
