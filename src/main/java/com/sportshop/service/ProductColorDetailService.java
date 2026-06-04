package com.sportshop.service;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.repository.DetailProductColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductColorDetailService {

    private final DetailProductColorRepository detailProductColorRepository;

    @Autowired
    public ProductColorDetailService(DetailProductColorRepository detailProductColorRepository) {
        this.detailProductColorRepository = detailProductColorRepository;
    }

    /**
     * Lấy toàn bộ chi tiết màu theo idProduct
     */
    public List<DetailProductColorDTO> getAllDetailProductColorsByProduct(Long idProduct) {
        List<DetailProductColorDTO> dtos = new ArrayList<>();
        List<DetailProductColorEntity> entities = detailProductColorRepository.findByIdIdProduct(idProduct);

        for (DetailProductColorEntity entity : entities) {
            DetailProductColorDTO dto = new DetailProductColorDTO();
            dto.setIdColor(entity.getId().getIdColor());
            dto.setIdProduct(entity.getId().getIdProduct());
            dto.setNameColor(entity.getDetailColor() != null ? entity.getDetailColor().getNameColor() : null);
            dto.setProductName(entity.getDetailColorProduct() != null ? entity.getDetailColorProduct().getName() : null);
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<DetailProductColorDTO> getAllDetailProductColors() {
        List<DetailProductColorEntity> entities = detailProductColorRepository.findAll();
        List<DetailProductColorDTO> dtos = new ArrayList<>();

        for (DetailProductColorEntity e : entities) {
            DetailProductColorDTO dto = new DetailProductColorDTO();
            dto.setIdColor(e.getId().getIdColor());
            dto.setIdProduct(e.getId().getIdProduct());
            dto.setNameColor(e.getDetailColor() != null ? e.getDetailColor().getNameColor() : null);
            dto.setProductName(e.getDetailColorProduct() != null ? e.getDetailColorProduct().getName() : null);
            dto.setCreatedAt(e.getCreatedAt());
            dto.setUpdatedAt(e.getUpdatedAt());
            dtos.add(dto);
        }

        return dtos;
    }

}
