//package com.sportshop.service.impl;
//
//import com.sportshop.converter.DetailProductColorConverter;
//import com.sportshop.dto.DetailProductColorDTO;
//import com.sportshop.entity.DetailProductColorEntity;
//import com.sportshop.entity.DetailProductColorId;
//import com.sportshop.repository.DetailProductColorRepository;
//import com.sportshop.service.IDetailProductColorService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class DetailProductColorService  implements IDetailProductColorService {
//    @Autowired
//    DetailProductColorRepository detailproductcolorRepository;
//    @Autowired
//    DetailProductColorConverter detailproductcolorConverter;
//
//    @Override
//    public List<DetailProductColorDTO> findAllDTO() {
//        List<DetailProductColorEntity> entities = detailproductcolorRepository.findAllIncludeNulls();
//
//        List<DetailProductColorDTO> result = new ArrayList<>();
//
//        for (DetailProductColorEntity e : entities) {
//            if (e == null) continue; // đề phòng null entity
//            DetailProductColorDTO dto = detailproductcolorConverter.toDTO(e);
//            result.add(dto);
//        }
//        return result;
//    }
//
//    @Override
//    public DetailProductColorDTO get( Long idColor, Long idProduct) {
//        // Tạo composite key từ 2 ID
//        DetailProductColorId id = new DetailProductColorId( idColor, idProduct);
//
//        // Gọi repository
//        DetailProductColorEntity detailcolor = detailproductcolorRepository.findOne(id);
//
//        // Nếu không tìm thấy thì trả null
//        if (detailcolor == null) {
//            return null;
//        }
//
//        // Convert sang DTO
//        return detailproductcolorConverter.toDTO(detailcolor);
//    }
//}
