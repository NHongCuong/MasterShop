//package com.sportshop.service.impl;
//
//import com.sportshop.converter.DetailProductMaterialConverter;
//import com.sportshop.dto.DetailProductMaterialDTO;
//import com.sportshop.entity.DetailProductMaterialEntity;
//import com.sportshop.entity.DetailProductMaterialId;
//import com.sportshop.repository.DetailProductMaterialRepository;
//import com.sportshop.service.IDetailProductMaterialService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class DetailProductMaterialService implements IDetailProductMaterialService {
//    @Autowired
//    DetailProductMaterialRepository detailproductmaterialRepository;
//    @Autowired
//    DetailProductMaterialConverter detailproductmaterialConverter;
//
//    @Override
//    public List<DetailProductMaterialDTO> findAllDTO() {
//        List<DetailProductMaterialEntity> entities = detailproductmaterialRepository.findAllIncludeNulls();
//
//        List<DetailProductMaterialDTO> result = new ArrayList<>();
//
//        for (DetailProductMaterialEntity e : entities) {
//            if (e == null) continue; // đề phòng null entity
//            DetailProductMaterialDTO dto = detailproductmaterialConverter.toDTO(e);
//            result.add(dto);
//        }
//        return result;
//    }
//    @Override
//    public DetailProductMaterialDTO get( Long idMaterial, Long idProduct) {
//        // Tạo composite key từ 2 ID
//        DetailProductMaterialId id = new DetailProductMaterialId( idMaterial, idProduct);
//
//        // Gọi repository
//        DetailProductMaterialEntity detailcolor = detailproductmaterialRepository.findOne(id);
//
//        // Nếu không tìm thấy thì trả null
//        if (detailcolor == null) {
//            return null;
//        }
//
//        // Convert sang DTO
//        return detailproductmaterialConverter.toDTO(detailcolor);
//    }
//}
