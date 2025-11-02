package com.sportshop.controller;

import com.sportshop.converter.CartDetailConverter;
import com.sportshop.dto.CartDetailDTO;
import com.sportshop.entity.*;
import com.sportshop.repository.CartDetailRepository;
import com.sportshop.repository.ProductRepository;
import com.sportshop.repository.ShopcartRepository;
import com.sportshop.service.ICartDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/cartdetail")
public class CartDetailController {
    @Autowired
    private ICartDetailService cartDetailService;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private CartDetailConverter cartDetailConverter;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopcartRepository shopcartRepository;


    @GetMapping("/all")
    public ResponseEntity<List<CartDetailDTO>> getAll() {
        List<CartDetailDTO> list = cartDetailService.findAllDTO();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCartDetail(@RequestBody CartDetailDTO dto) {
        try {
            // kiểm tra foreign key hợp lệ
            if (!productRepository.existsById(dto.getIdProduct())) {
                return ResponseEntity.badRequest().body("Product ID không tồn tại: " + dto.getIdProduct());
            }
            if (!shopcartRepository.existsById(dto.getIdSC())) {
                return ResponseEntity.badRequest().body("Shopcart ID không tồn tại: " + dto.getIdSC());
            }

            CartDetailEntity entity = cartDetailConverter.toEntity(dto);
            entity.setId(new CartDetailId(dto.getIdSC(), dto.getIdProduct()));

            cartDetailRepository.save(entity);
            return ResponseEntity.ok(cartDetailConverter.toDTO(entity));

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addcart")
    public ResponseEntity<?> addCartDetails(@RequestBody CartDetailDTO dto) {
        try {
            if (dto.getIdSC() == null || dto.getIdProduct() == null) {
                return ResponseEntity.badRequest().body("Thiếu ID_SC hoặc ID_Product");
            }

            // --- Khởi tạo entity và ID phức hợp ---
            CartDetailEntity entity = new CartDetailEntity();
            entity.setId(new CartDetailId(dto.getIdSC(), dto.getIdProduct()));
            entity.setAmountCD(dto.getAmountCD());
            entity.setCreatedAt(dto.getCreatedAt());
            entity.setUpdatedAt(dto.getUpdatedAt());
            entity.setIdColor(dto.getIdColor());

            // --- Gán liên kết bắt buộc ---
            ShopcartEntity sc = new ShopcartEntity();
            sc.setId(dto.getIdSC());
            entity.setShopCartDetail(sc);

            ProductEntity product = new ProductEntity();
            product.setId(dto.getIdProduct());
            entity.setProductCartDetail(product);

            // --- Gán liên kết tùy chọn (nếu có) ---
            if (dto.getIdMaterial() != null) {
                MaterialEntity material = new MaterialEntity();
                material.setId(dto.getIdMaterial());
                entity.setMaterialCartDetail(material);
            }

            if (dto.getIdD() != null) {
                DimensionsEntity dim = new DimensionsEntity();
                dim.setId(dto.getIdD());
                entity.setDemensionsCartDetail(dim);
            }

            // --- Lưu ---
            CartDetailEntity saved = cartDetailRepository.save(entity);
            return ResponseEntity.ok(cartDetailConverter.toDTO(saved));

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

