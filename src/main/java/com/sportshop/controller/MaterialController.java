package com.sportshop.controller;

import com.sportshop.entity.MaterialEntity;
import com.sportshop.repository.MaterialRepository;
import com.sportshop.service.IMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/material")
public class MaterialController {
    @Autowired
    IMaterialService materialService;
    @Autowired
    private MaterialRepository materialRepo;

    @GetMapping("/all")
    public ResponseEntity<?> getAllMaterial() {
        try {
            return new ResponseEntity<>(materialRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialEntity> getMaterialById(@PathVariable("id") Long id) {
        MaterialEntity material = materialRepo.findById(id).orElse(null); // ✅ Spring 1.5 dùng findOne
        if (material == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(material);
    }
}
