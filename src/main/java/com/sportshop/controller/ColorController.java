package com.sportshop.controller;

import com.sportshop.entity.ColorEntity;
import com.sportshop.repository.ColorRepository;
import com.sportshop.service.IColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/color")
public class ColorController {
    @Autowired
    IColorService colorService;
    @Autowired
    private ColorRepository colorRepo;

    @GetMapping("/all")
    public ResponseEntity<?> getAllColor() {
        try {
            return new ResponseEntity<>(colorRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColorEntity> getColorById(@PathVariable("id") Long id) {
        ColorEntity color = colorRepo.findOne(id); // ✅ Spring 1.5 dùng findOne
        if (color == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(color);
    }
}
