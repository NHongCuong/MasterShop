package com.sportshop.controller;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.dto.OrderDetailDTO;
import com.sportshop.entity.OrderDetailEntity;
import com.sportshop.entity.OrderDetailId;
import com.sportshop.entity.OrderEntity;
import com.sportshop.entity.ShopcartEntity;
import com.sportshop.repository.OrderDetailRepository;
import com.sportshop.repository.OrderRepository;
import com.sportshop.repository.ShopcartRepository;
import com.sportshop.service.OrderDetailService;
import com.sportshop.service.ProductColorDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/orderdetail")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ShopcartRepository shopcartRepository;

//    @Autowired
//    private DetailProductColorRepository dtailProductColorRepository;

    public OrderDetailController(OrderDetailService orderDetailService,
                                 OrderDetailRepository orderDetailRepository,
                                 OrderRepository orderRepository,
                                 ShopcartRepository shopcartRepository) {
        this.orderDetailService = orderDetailService;
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.shopcartRepository = shopcartRepository;
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

    @PostMapping("/add")
    public ResponseEntity<?> addOrderDetail(@RequestBody OrderDetailDTO dto) {
        try {
            if (dto.getIdOrder() == null || dto.getIdSC() == null) {
                return ResponseEntity.badRequest().body("Thiếu ID_Order hoặc ID_SC");
            }

            // Validate foreign keys exist
            if (!orderRepository.exists(dto.getIdOrder())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Order ID không tồn tại: " + dto.getIdOrder());
            }
            if (!shopcartRepository.existsById(dto.getIdSC())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Shopcart ID không tồn tại: " + dto.getIdSC());
            }

            OrderDetailEntity entity = new OrderDetailEntity();
            entity.setId(new OrderDetailId(dto.getIdOrder(), dto.getIdSC()));

            OrderEntity order = new OrderEntity();
            order.setId(dto.getIdOrder());
            entity.setOrderEntity(order);

            ShopcartEntity sc = new ShopcartEntity();
            sc.setId(dto.getIdSC());
            entity.setShopcartOD(sc);

            OrderDetailEntity saved = orderDetailRepository.save(entity);
            return ResponseEntity.ok(new OrderDetailDTO(
                    saved.getId().getIdOrder(),
                    saved.getId().getIdSC()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm order detail: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
