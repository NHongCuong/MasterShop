package com.sportshop.controller;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.dto.OrderDetailDTO;
import com.sportshop.service.OrderDetailService;
import com.sportshop.service.ProductColorDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/orderdetail")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

//    @Autowired
//    private DetailProductColorRepository dtailProductColorRepository;

    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }
    @GetMapping("/all")
    public ResponseEntity<List<OrderDetailDTO>> getAllOrderDetail() {
        List<OrderDetailDTO> dtos = orderDetailService.getAllOrderDetailSC();
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
    /**
     * API: GET /detailcolor/all/{idProduct}
     */
    @GetMapping("/all/{idSC}")
    public ResponseEntity<List<OrderDetailDTO>> getAllOrderDetailByIdSC(@PathVariable Long idSC) {
        List<OrderDetailDTO> dtos = orderDetailService.getAllOrderDetailBySC(idSC);
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
}
