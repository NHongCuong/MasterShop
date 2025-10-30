//package com.sportshop.controller;
//
//
//import com.sportshop.dto.DetailProductColorDTO;
//import com.sportshop.entity.DetailProductColorEntity;
//import com.sportshop.entity.DetailProductColorId;
//import com.sportshop.repository.DetailProductColorRepository;
//import com.sportshop.service.IDetailProductColorService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//@CrossOrigin
//@RestController
//@RequestMapping("/detailcolor")
//public class DetailProductColorController {
//    @Autowired
//    private IDetailProductColorService detailproductcolorService;
//
//    @Autowired
//    private DetailProductColorRepository detailproductcolorRepository;
//
//
//    @GetMapping("/all")
//    public ResponseEntity<List<DetailProductColorDTO>> getAll() {
//        List<DetailProductColorDTO> list = detailproductcolorService.findAllDTO();
//        return ResponseEntity.ok(list);
//    }
//
//    @GetMapping("{idColor}/{idProduct}")
//    public ResponseEntity<DetailProductColorEntity> getDetailColorById(
//            @PathVariable Long idColor,
//            @PathVariable Long idProduct) {
//
//        DetailProductColorId id = new DetailProductColorId(idColor, idProduct);
//        DetailProductColorEntity color = detailproductcolorRepository.findOne(id);
//        if (color == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(color);
//    }
//
//
//}
