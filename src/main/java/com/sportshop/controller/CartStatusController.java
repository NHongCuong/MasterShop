package com.sportshop.controller;

import com.sportshop.entity.CartStatusEntity;
import com.sportshop.repository.CartStatusRepository;
import com.sportshop.service.ICartStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/cartstatus")
public class CartStatusController {
    @Autowired
    ICartStatusService cartstatusService;
    @Autowired
    private CartStatusRepository cartstatusRepo;

    @GetMapping("/all")
    public ResponseEntity<?> getAllCartStatus() {
        try {
            return new ResponseEntity<>(cartstatusRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartStatusEntity> getCartStatusById(@PathVariable("id") Long id) {
        CartStatusEntity cartstatus = cartstatusRepo.findOne(id); // ✅ Spring 1.5 dùng findOne
        if (cartstatus == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cartstatus);
    }
}
