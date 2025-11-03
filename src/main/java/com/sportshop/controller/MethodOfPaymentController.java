package com.sportshop.controller;

import com.sportshop.entity.MethodOfPaymentEntity;
import com.sportshop.repository.MethodOfPaymentRepository;
import com.sportshop.service.IMethodOfPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/methodofpayment")
public class MethodOfPaymentController {
    @Autowired
    IMethodOfPaymentService methodOfPaymentService;
    @Autowired
    private MethodOfPaymentRepository methodOfPaymentRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllMethodOfPayment() {
        try {
            return new ResponseEntity<>(methodOfPaymentRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MethodOfPaymentEntity> getMethodOfPaymentById(@PathVariable("id") Long id) {
        MethodOfPaymentEntity methodOfPayment = methodOfPaymentRepository.findOne(id); // ✅ Spring 1.5 dùng findOne
        if (methodOfPayment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(methodOfPayment);
    }
}
