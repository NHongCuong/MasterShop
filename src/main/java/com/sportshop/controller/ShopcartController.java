package com.sportshop.controller;

import com.sportshop.dto.ShopcartDTO;
import com.sportshop.entity.CategoryEntity;
import com.sportshop.entity.ShopcartEntity;
import com.sportshop.repository.CategoryRepository;
import com.sportshop.repository.ShopcartRepository;
import com.sportshop.service.ICategoryService;
import com.sportshop.service.IShopcartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/cart")
public class ShopcartController {
    @Autowired
    IShopcartService shopcartService;
    @Autowired
    private ShopcartRepository shopcartRepo;

    @GetMapping("/all")
    public ResponseEntity<?> getAllShopcarts() {
        try {
            return new ResponseEntity<>(shopcartRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ShopcartEntity> getShopcartById(@PathVariable("id") Long id) {
        ShopcartEntity shopcart = shopcartRepo.findOne(id); // ✅ Spring 1.5 dùng findOne
        if (shopcart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shopcart);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addShopcart(@RequestBody ShopcartEntity shopcartEntity) {
        try{
            ShopcartEntity shopcart = shopcartRepo.save(shopcartEntity);
            return new ResponseEntity<>(shopcart,HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm shop cart:" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
