package com.sportshop.controller;

import com.sportshop.dto.DimensionsDTO;
import com.sportshop.service.IDimensionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/dimensions")
public class ProductDimensionsDetailController {

    @Autowired
    private IDimensionsService dimensionsService;

    @GetMapping("/all/{idProduct}")
    public ResponseEntity<List<DimensionsDTO>> getAllByProduct(@PathVariable Long idProduct) {
        List<DimensionsDTO> dtos = dimensionsService.findByProductId(idProduct);

        if (dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
}
