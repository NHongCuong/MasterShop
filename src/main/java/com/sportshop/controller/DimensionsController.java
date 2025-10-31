package com.sportshop.controller;

import com.sportshop.dto.DimensionsDTO;
import com.sportshop.service.DimensionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/dimensions")
public class DimensionsController {
    private final DimensionsService service;

    public DimensionsController(DimensionsService service) {
        this.service = service;
    }

    /**
     * ✅ API: GET /dimensions/all/{idProduct}
     * Lấy danh sách kích thước theo idProduct
     */
    @GetMapping("/all/{idProduct}")
    public ResponseEntity<List<DimensionsDTO>> getAllDimensionsByProduct(@PathVariable Long idProduct) {
        List<DimensionsDTO> dtos = service.getAllDimensionsByProduct(idProduct);
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }

    /**
     * ✅ API: GET /dimensions/all
     * Lấy tất cả kích thước
     */
    @GetMapping("/all")
    public ResponseEntity<List<DimensionsDTO>> getAllDimensions() {
        List<DimensionsDTO> dtos = service.getAllDimensions();
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
}
