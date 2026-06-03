package com.sportshop.controller;

import com.sportshop.dto.CartDetailDTO;
import com.sportshop.repository.CartDetailRepository;
import com.sportshop.service.ICartDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/cartdetail")
public class CartDetailController {
    @Autowired
    private ICartDetailService cartDetailService;

    @GetMapping("/all")
    public ResponseEntity<List<CartDetailDTO>> getAll() {
        List<CartDetailDTO> list = cartDetailService.findAllDTO();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartDetailDTO>> getByUserId(@PathVariable Long userId) {
        List<CartDetailDTO> list = cartDetailService.findActiveItemsByUserId(userId);
        return ResponseEntity.ok(list);
    }
}
