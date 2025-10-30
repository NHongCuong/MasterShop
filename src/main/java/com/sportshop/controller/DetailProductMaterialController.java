//package com.sportshop.controller;
//
//import com.sportshop.dto.DetailProductMaterialDTO;
//import com.sportshop.entity.DetailProductMaterialEntity;
//import com.sportshop.entity.DetailProductMaterialId;
//import com.sportshop.repository.DetailProductMaterialRepository;
//import com.sportshop.service.IDetailProductMaterialService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//
//import java.util.List;
//
//@CrossOrigin
//@RestController
//@RequestMapping("/detailmaterial")
//public class DetailProductMaterialController {
//    @Autowired
//    private IDetailProductMaterialService detailproductmaterialService;
//
//    @Autowired
//    private DetailProductMaterialRepository detailproductmaterialRepository;
//
//
//    @GetMapping("/all")
//    public ResponseEntity<List<DetailProductMaterialDTO>> getAll() {
//        List<DetailProductMaterialDTO> list = detailproductmaterialService.findAllDTO();
//        return ResponseEntity.ok(list);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<DetailProductMaterialEntity> getDetailColorById(@PathVariable("id") DetailProductMaterialId id) {
//        DetailProductMaterialEntity color = detailproductmaterialRepository.findOne(id); // ✅ Spring 1.5 dùng findOne
//        if (color == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(color);
//    }
//
//    @GetMapping("{idMaterial}/{idProduct}")
//    public ResponseEntity<DetailProductMaterialEntity> getDetailMaterialById(
//            @PathVariable Long idMaterial,
//            @PathVariable Long idProduct) {
//
//        DetailProductMaterialId id = new DetailProductMaterialId(idMaterial, idProduct);
//        DetailProductMaterialEntity material = detailproductmaterialRepository.findOne(id);
//        if (material == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(material);
//    }
//}
//
