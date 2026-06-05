package com.sportshop.converter;

import com.sportshop.dto.CartDetailDTO;
import com.sportshop.entity.*;
import org.springframework.stereotype.Component;

@Component
public class CartDetailConverter {

    // ✅ Convert DTO -> Entity
    public CartDetailEntity toEntity(CartDetailDTO dto) {
        if (dto == null) return null;

        CartDetailEntity entity = new CartDetailEntity();
        entity.setIdCartDetail(dto.getIdCartDetail());

        // --- Liên kết bắt buộc ---
        if (dto.getIdSC() != null) {
            ShopcartEntity sc = new ShopcartEntity();
            sc.setId(dto.getIdSC());
            entity.setShopcartdetail(sc);
        }

        if (dto.getIdProduct() != null) {
            ProductEntity product = new ProductEntity();
            product.setId(dto.getIdProduct());
            product.setName(dto.getProductName());
            entity.setProductcartdetail(product);
        }

        // --- Các liên kết tùy chọn ---
        if (dto.getIdMaterial() != null) {
            MaterialEntity material = new MaterialEntity();
            material.setId(dto.getIdMaterial());
            entity.setMaterialcartdetail(material);
        }

        if (dto.getIdD() != null) {
            DimensionsEntity dim = new DimensionsEntity();
            dim.setId(dto.getIdD());
            entity.setDemensionsCartDetail(dim);
        }

        if (dto.getIdColor() != null) {
            ColorEntity col = new ColorEntity();
            col.setId(dto.getIdColor());
            entity.setColorEntity(col);
        }

        // --- Các cột cơ bản ---
        entity.setAmountCD(dto.getAmountCD());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

    // ✅ Convert Entity -> DTO (null-safe)
    public CartDetailDTO toDTO(CartDetailEntity entity) {
        if (entity == null) return null;

        CartDetailDTO dto = new CartDetailDTO();
        dto.setIdCartDetail(entity.getIdCartDetail());

        if (entity.getShopcartdetail() != null) {
            dto.setIdSC(entity.getShopcartdetail().getId());
        }

        if (entity.getProductcartdetail() != null) {
            dto.setIdProduct(entity.getProductcartdetail().getId());
            dto.setProductName(entity.getProductcartdetail().getName());
            dto.setProductPrice(entity.getProductcartdetail().getPrice());
            dto.setProductAvatar(entity.getProductcartdetail().getAvatar());
        }

        // --- Lấy thông tin các entity liên kết (có thể null) ---
        if (entity.getMaterialcartdetail() != null) {
            dto.setIdMaterial(entity.getMaterialcartdetail().getId());
            dto.setMaterialName(entity.getMaterialcartdetail().getNameMaterial());
        }

        if (entity.getDemensionsCartDetail() != null) {
            dto.setIdD(entity.getDemensionsCartDetail().getId());
            dto.setDimensionName(entity.getDemensionsCartDetail().getNameD());
        }

        if (entity.getColorEntity() != null) {
            dto.setIdColor(entity.getColorEntity().getId());
            dto.setColorName(entity.getColorEntity().getNameColor());
        }

        // --- Các trường cơ bản ---
        dto.setAmountCD(entity.getAmountCD());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
