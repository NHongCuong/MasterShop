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

        // --- ID phức hợp ---
        CartDetailId id = new CartDetailId(dto.getIdSC(), dto.getIdProduct());
        entity.setId(id);

        // --- Liên kết bắt buộc ---
        if (dto.getIdSC() != null) {
            ShopcartEntity sc = new ShopcartEntity();
            sc.setId(dto.getIdSC());

            entity.setCartsDetail(sc);
        }

        if (dto.getIdProduct() != null) {
            ProductEntity product = new ProductEntity();
            product.setId(dto.getIdProduct());
            product.setName(dto.getProductName());
            entity.setOrderDetail(product);
        }

        // --- Các liên kết tùy chọn ---
        if (dto.getIdMaterial() != null) {
            MaterialEntity material = new MaterialEntity();
            material.setId(dto.getIdMaterial());
            material.setNameMaterial(dto.getMaterialName());
            entity.setCartDetail(material);
        }

        if (dto.getIdD() != null) {
            DimensionsEntity dim = new DimensionsEntity();
            dim.setId(dto.getIdD());
            dim.setNameD(dto.getDimensionName());
            entity.setDemensionsCartDetail(dim);
        }

        // --- Các cột cơ bản ---
        entity.setIdColor(dto.getIdColor());
        entity.setAmountCD(dto.getAmountCD());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

    // ✅ Convert Entity -> DTO (null-safe)
    public CartDetailDTO toDTO(CartDetailEntity entity) {
        if (entity == null) return null;

        CartDetailDTO dto = new CartDetailDTO();

        // --- Lấy ID từ composite key ---
        if (entity.getId() != null) {
            dto.setIdSC(entity.getId().getIdSC());
            dto.setIdProduct(entity.getId().getIdProduct());
            dto.setProductName(entity.getOrderDetail().getName());

        }

        // --- Lấy thông tin các entity liên kết (có thể null) ---
        dto.setIdMaterial(
                entity.getCartDetail() != null ? entity.getCartDetail().getId() : null

        );

        dto.setIdD(
                entity.getDemensionsCartDetail() != null ? entity.getDemensionsCartDetail().getId() : null
        );
        dto.setMaterialName(entity.getCartDetail() != null ? entity.getCartDetail().getNameMaterial() : null);
        dto.setDimensionName(entity.getDemensionsCartDetail() != null ? entity.getDemensionsCartDetail().getNameD() : null);
        dto.setIdColor(entity.getIdColor());

        // --- Các trường cơ bản ---
        dto.setAmountCD(entity.getAmountCD());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
