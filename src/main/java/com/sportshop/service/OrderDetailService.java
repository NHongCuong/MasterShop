package com.sportshop.service;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.dto.OrderDetailDTO;
import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.OrderDetailEntity;
import com.sportshop.repository.DetailProductColorRepository;
import com.sportshop.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;

    @Autowired
    public OrderDetailService(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }

    /**
     * Lấy toàn bộ chi tiết màu theo idProduct
     */
    public List<OrderDetailDTO> getAllOrderDetailBySC(Long idSC) {
        List<OrderDetailDTO> dtos = new ArrayList<>();

        // Giả sử repository có hàm: findByProduct_Id(Long idProduct)
        List<OrderDetailEntity> entities = orderDetailRepository.findById_IdSC(idSC);

        for (OrderDetailEntity entity : entities) {

           OrderDetailDTO dto = new OrderDetailDTO(
                    entity.getId().getIdOrder(),
                    entity.getId().getIdSC()

            );
            dtos.add(dto);
        }
        return dtos;
    }
    public List<OrderDetailDTO> getAllOrderDetailSC() {
        List<OrderDetailEntity> entities = orderDetailRepository.findAll();
        List<OrderDetailDTO> dtos = new ArrayList<>();

        for (OrderDetailEntity e : entities) {
            dtos.add(new OrderDetailDTO(
                    e.getId().getIdOrder(),
                    e.getId().getIdSC()
            ));
        }

        return dtos;
    }
}
