package com.sportshop.controller;

import com.sportshop.entity.DimensionsEntity;
import com.sportshop.repository.DimensionsRepository;
import com.sportshop.service.IDimensionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/dimensions")
public class DimensionsController {
    @Autowired
    IDimensionsService demensionsService;
    @Autowired
    private DimensionsRepository dimensionsRepo;

    @GetMapping("/all")
    public ResponseEntity<?> getAllDimensions() {
        try {
            return new ResponseEntity<>(dimensionsRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all/{productId}")
    public ResponseEntity<java.util.List<DimensionsEntity>> getAllByProductId(@PathVariable Long productId) {
        java.util.List<DimensionsEntity> list = dimensionsRepo.findByDemensions_Id(productId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DimensionsEntity> getDimensionsById(@PathVariable("id") Long id) {
        DimensionsEntity dimensions = dimensionsRepo.findById(id).orElse(null); // ✅ Spring 1.5 dùng findOne
        if (dimensions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dimensions);
    }
}
