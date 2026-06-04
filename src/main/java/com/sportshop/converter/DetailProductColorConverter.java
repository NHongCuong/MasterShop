package com.sportshop.converter;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DetailProductColorConverter {
    // ✅ Convert DTO -> Entity
    public DetailProductColorEntity toEntity(DetailProductColorDTO dto) {
        if (dto == null) return null;

        DetailProductColorEntity entity = new DetailProductColorEntity();

        // --- ID phức hợp ---
        DetailProductColorId id = new DetailProductColorId(dto.getIdColor(), dto.getIdProduct());
        entity.setId(id);

        // --- Liên kết bắt buộc ---
        if (dto.getIdColor() != null) {
            ColorEntity cl = new ColorEntity();
            cl.setId(dto.getIdColor());
            cl.setNameColor(dto.getNameColor());

            entity.setDetailColor(cl);
        }

        if (dto.getIdProduct() != null) {
            ProductEntity product = new ProductEntity();
            product.setId(dto.getIdProduct());
            product.setName(dto.getProductName());
            entity.setDetailColorProduct(product);
        }
        
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

    // ✅ Convert Entity -> DTO (null-safe)
    public DetailProductColorDTO toDTO(DetailProductColorEntity entity) {
        if (entity == null) return null;

        DetailProductColorDTO dto = new DetailProductColorDTO();

        // --- Lấy ID từ composite key ---
        if (entity.getId() != null) {
            dto.setIdColor(entity.getId().getIdColor());
            dto.setIdProduct(entity.getId().getIdProduct());
        }
        
        if (entity.getDetailColorProduct() != null) {
            dto.setProductName(entity.getDetailColorProduct().getName());
        }
        
        if (entity.getDetailColor() != null) {
            dto.setNameColor(entity.getDetailColor().getNameColor());
        }
        
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
