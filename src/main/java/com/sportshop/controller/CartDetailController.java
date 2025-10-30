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

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @GetMapping("/all")
    public ResponseEntity<List<CartDetailDTO>> getAll() {
        List<CartDetailDTO> list = cartDetailService.findAllDTO();
        return ResponseEntity.ok(list);
    }

}
