package com.sportshop.controller;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.service.ProductColorDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/detailcolor")
public class ProductColorDetailController {

    private final ProductColorDetailService service;

//    @Autowired
//    private DetailProductColorRepository dtailProductColorRepository;

    public ProductColorDetailController(ProductColorDetailService service) {
        this.service = service;
    }
    @GetMapping("/all")
    public ResponseEntity<List<DetailProductColorDTO>> getAllDetailColors() {
        List<DetailProductColorDTO> dtos = service.getAllDetailProductColors();
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
    /**
     * API: GET /detailcolor/all/{idProduct}
     */
    @GetMapping("/all/{idProduct}")
    public ResponseEntity<List<DetailProductColorDTO>> getAllDetailColors(@PathVariable Long idProduct) {
        List<DetailProductColorDTO> dtos = service.getAllDetailProductColorsByProduct(idProduct);
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtos);
    }
}
