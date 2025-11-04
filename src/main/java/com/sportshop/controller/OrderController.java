package com.sportshop.controller;

import com.sportshop.entity.*;
import com.sportshop.repository.MethodOfPaymentRepository;
import com.sportshop.repository.OrderRepository;
import com.sportshop.repository.ShipMethodRepository;
import com.sportshop.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    IOrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShipMethodRepository shipMethodRepository;

    @Autowired
    private MethodOfPaymentRepository methodOfPaymentRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrder() {
        try {
            return new ResponseEntity<>(orderRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderEntity> getOrderById(@PathVariable("id") Long id) {
        OrderEntity order = orderRepository.findOne(id); // ✅ Spring 1.5 dùng findOne
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{add}")
    public ResponseEntity<?> addOrder(@RequestBody OrderEntity orderEntity){
        try{
            if (orderEntity.getShipMethod() != null && orderEntity.getShipMethod().getId() != null) {
                ShipMethodEntity shipmethod = shipMethodRepository.findOne(orderEntity.getShipMethod().getId());
                orderEntity.setShipMethod(shipmethod);
            } else {
                // Nếu không có category, có thể gán null hoặc category mặc định
                orderEntity.setShipMethod(null);
            }

            // Gán supplier (nếu có ID)
            if (orderEntity.getMethodofPayment() != null && orderEntity.getMethodofPayment().getId() != null) {
                MethodOfPaymentEntity methodOfPayment = methodOfPaymentRepository.findOne(orderEntity.getMethodofPayment().getId());
                orderEntity.setMethodofPayment(methodOfPayment);
            } else {
                orderEntity.setMethodofPayment(null);
            }
            OrderEntity order = orderRepository.save(orderEntity);
            return new ResponseEntity<>(order,HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm đơn hàng:" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
