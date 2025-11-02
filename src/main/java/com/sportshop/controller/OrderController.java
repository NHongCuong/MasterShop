package com.sportshop.controller;

import com.sportshop.entity.CategoryEntity;
import com.sportshop.entity.OrderEntity;
import com.sportshop.repository.OrderRepository;
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
            OrderEntity order = orderRepository.save(orderEntity);
            return new ResponseEntity<>(order,HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm đơn hàng:" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
