package com.sportshop.controller;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.dto.DetailProductMaterialDTO;
import com.sportshop.service.ProductColorDetailService;
import com.sportshop.service.ProductMaterialDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/detailmaterial")
public class ProductMaterialDetailController {
    private final ProductMaterialDetailService service;

    public ProductMaterialDetailController(ProductMaterialDetailService service) {
        this.service = service;
    }
    @GetMapping("/all")
    public ResponseEntity<List<DetailProductMaterialDTO>> getAllDetailMaterials() {
        List<DetailProductMaterialDTO> dtos = service.getAllDetailProductMaterials();
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
    /**
     * API: GET /detailco/all/{idProduct}
     */
    @GetMapping("/all/{idProduct}")
    public ResponseEntity<List<DetailProductMaterialDTO>> getAllDetailMaterials(@PathVariable Long idProduct) {
        List<DetailProductMaterialDTO> dtos = service.getAllDetailProductMaterialsByProduct(idProduct);
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
}
