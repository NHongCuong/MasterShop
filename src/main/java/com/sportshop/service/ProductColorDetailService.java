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

        // Giả sử repository có hàm: findByProduct_Id(Long idProduct)
        List<DetailProductColorEntity> entities = detailProductColorRepository.findByIdIdProduct(idProduct);

        for (DetailProductColorEntity entity : entities) {
            String nameColor = null;
            if (entity.getDetailColor() != null) {
                nameColor = entity.getDetailColor().getNameColor();
            }

            DetailProductColorDTO dto = new DetailProductColorDTO(
                    entity.getId().getIdColor(),
                    entity.getId().getIdProduct(),
                    nameColor
            );
            dtos.add(dto);
        }
        return dtos;
    }
    public List<DetailProductColorDTO> getAllDetailProductColors() {
        List<DetailProductColorEntity> entities = detailProductColorRepository.findAll();
        List<DetailProductColorDTO> dtos = new ArrayList<>();

        for (DetailProductColorEntity e : entities) {
            dtos.add(new DetailProductColorDTO(
                    e.getId().getIdColor(),
                    e.getId().getIdProduct(),
                    e.getDetailColor() != null ? e.getDetailColor().getNameColor() : null
            ));
        }

        return dtos;
    }

}
