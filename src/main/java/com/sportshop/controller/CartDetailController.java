package com.sportshop.controller;

import com.sportshop.dto.CartDetailDTO;
import com.sportshop.entity.CartDetailEntity;
import com.sportshop.entity.CategoryEntity;
import com.sportshop.repository.CartDetailRepository;
import com.sportshop.service.ICartDetailService;
import com.sportshop.service.impl.CartDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


//    @GetMapping("/all")
//    public List<CartDetailDTO> getAll() {
//
//        return cartDetailService.getAll();
//    }
    @GetMapping("/all")
    public ResponseEntity<List<CartDetailDTO>> getAll() {
        List<CartDetailDTO> list = cartDetailService.findAllDTO();
        return ResponseEntity.ok(list);
    }

}
