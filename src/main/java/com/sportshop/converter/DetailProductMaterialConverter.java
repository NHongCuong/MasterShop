//package com.sportshop.converter;
//
//import com.sportshop.dto.DetailProductMaterialDTO;
//import com.sportshop.entity.*;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DetailProductMaterialConverter {
//    public DetailProductMaterialEntity toEntity(DetailProductMaterialDTO dto) {
//        if (dto == null) return null;
//
//        DetailProductMaterialEntity entity = new DetailProductMaterialEntity();
//
//        // --- ID phức hợp ---
//        DetailProductMaterialId id = new DetailProductMaterialId(dto.getIdMaterial(), dto.getIdProduct());
//        entity.setId(id);
//
//        // --- Liên kết bắt buộc ---
//        if (dto.getIdMaterial() != null) {
//            MaterialEntity mt = new MaterialEntity();
//            mt.setId(dto.getIdMaterial());
//            mt.setNameMaterial(dto.getMaterialName());
//
//            entity.setDetailMaterial(mt);
//        }
//
//        if (dto.getIdProduct() != null) {
//            ProductEntity product = new ProductEntity();
//            product.setId(dto.getIdProduct());
//            product.setName(dto.getProductName());
//            entity.setDetailMaterialProduct(product);
//        }
//
//        return entity;
//    }
//
//    // ✅ Convert Entity -> DTO (null-safe)
//    public DetailProductMaterialDTO toDTO(DetailProductMaterialEntity entity) {
//        if (entity == null) return null;
//
//        DetailProductMaterialDTO dto = new DetailProductMaterialDTO();
//
//        // --- Lấy ID từ composite key ---
//        if (entity.getId() != null) {
//            dto.setIdMaterial(entity.getId().getIdMaterial());
//            dto.setIdProduct(entity.getId().getIdProduct());
//            dto.setProductName(entity.getDetailMaterialProduct().getName());
//            dto.setMaterialName(entity.getDetailMaterial().getNameMaterial());
//
//        }
//
//        return dto;
//    }
//}
