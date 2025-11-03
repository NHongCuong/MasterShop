package com.sportshop.controller;

import com.sportshop.entity.ShipMethodEntity;
import com.sportshop.repository.ShipMethodRepository;
import com.sportshop.service.IShipMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/shipmethod")
public class ShipMethodController {
    @Autowired
    IShipMethodService shipMethodService;
    @Autowired
    private ShipMethodRepository shipMethodRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllShipMethods() {
        try {
            return new ResponseEntity<>(shipMethodRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipMethodEntity> getShipMethodById(@PathVariable("id") Long id) {
        ShipMethodEntity shipmethod = shipMethodRepository.findOne(id); // ✅ Spring 1.5 dùng findOne
        if (shipmethod == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shipmethod);
    }
}
