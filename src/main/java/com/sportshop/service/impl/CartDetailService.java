package com.sportshop.service.impl;


import com.sportshop.converter.CartDetailConverter;
import com.sportshop.dto.CartDetailDTO;
import com.sportshop.entity.CartDetailEntity;
import com.sportshop.entity.CartDetailId;
import com.sportshop.repository.CartDetailRepository;
import com.sportshop.service.ICartDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
public class CartDetailService implements ICartDetailService {
    @Autowired
    CartDetailRepository cartDetailRepository;
    @Autowired
    CartDetailConverter cartDetailConverter;
//    @Override
//    public List<CartDetailDTO> getAll() {
//        List<CartDetailEntity> list = cartDetailRepository.findAll();
//        System.out.println("DEBUG: cart_detail size = " + list.size());
//
//        List<CartDetailDTO> result = new ArrayList<>();
//        for (CartDetailEntity en : list) {
//            System.out.println("DEBUG: entity = " + en + " id=" + (en!=null && en.getId()!=null ? en.getId().getIdSC() +"/"+en.getId().getIdProduct() : "null"));
//            CartDetailDTO dto = cartDetailConverter.toDTO(en); // billConverter = cartDetailConverter bean
//            //if (dto != null) {
//                result.add(dto);
//            //} else {
//                // optional: log the null conversion
//                System.out.println("WARN: converter returned null for entity " + en);
//            //}
//        }
//        return result;
//    }
    @Override
    public List<CartDetailDTO> findAllDTO() {
        List<CartDetailEntity> entities = cartDetailRepository.findAllIncludeNulls();

        List<CartDetailDTO> result = new ArrayList<>();

        for (CartDetailEntity e : entities) {
            if (e == null) continue; // đề phòng null entity
            CartDetailDTO dto = cartDetailConverter.toDTO(e);
            result.add(dto);
        }
        return result;
    }
    @Override
    public CartDetailDTO get(CartDetailId id) {
        CartDetailEntity bill = cartDetailRepository.findOne(id);
        CartDetailDTO dto = cartDetailConverter.toDTO(bill);
        return dto;
    }
}
