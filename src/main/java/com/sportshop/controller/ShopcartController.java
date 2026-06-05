package com.sportshop.controller;

import com.sportshop.entity.*;
import com.sportshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/cart")
public class ShopcartController {
    @Autowired
    private ShopcartRepository shopcartRepo;

    @Autowired
    private CartDetailRepository cartDetailRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CartStatusRepository cartStatusRepo;

    @Autowired
    private MaterialRepository materialRepo;

    @Autowired
    private DimensionsRepository dimensionsRepo;

    @Autowired
    private ColorRepository colorRepo;


    @GetMapping("/all")
    public ResponseEntity<?> getAllShopcarts() {
        try {
            return new ResponseEntity<>(shopcartRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ShopcartEntity> getShopcartById(@PathVariable("id") Long id) {
        ShopcartEntity shopcart = shopcartRepo.findById(id).orElse(null); // ✅ Spring 1.5 dùng findOne
        if (shopcart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shopcart);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addShopcart(@RequestBody ShopcartEntity shopcartEntity) {
        try{
            ShopcartEntity shopcart = shopcartRepo.save(shopcartEntity);
            return new ResponseEntity<>(shopcart,HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm shop cart:" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add-item")
    public ResponseEntity<?> addItem(@RequestBody java.util.Map<String, Object> payload) {
        try {
            if (payload.get("userId") == null || payload.get("productId") == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin User hoặc Sản phẩm");
            }

            Long userId = Long.valueOf(payload.get("userId").toString());
            Long productId = Long.valueOf(payload.get("productId").toString());
            Long amount = payload.get("amount") != null ? Long.valueOf(payload.get("amount").toString()) : 1L;
            
            Long colorId = payload.get("colorId") != null ? Long.valueOf(payload.get("colorId").toString()) : null;
            Long materialId = payload.get("materialId") != null ? Long.valueOf(payload.get("materialId").toString()) : null;
            Long dimensionId = payload.get("dimensionId") != null ? Long.valueOf(payload.get("dimensionId").toString()) : null;

            // 1. Tìm hoặc tạo Shopcart cho user (status = 3 - Buying)
            java.util.List<ShopcartEntity> carts = shopcartRepo.findByUserSC_IdAndCartStatus_Id(userId, 3L);
            ShopcartEntity cart = (carts != null && !carts.isEmpty()) ? carts.get(0) : null;
            
            if (cart == null) {
                cart = new ShopcartEntity();
                UserEntity user = userRepo.findById(userId).orElse(null);
                if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
                
                CartStatusEntity status = cartStatusRepo.findById(3L).orElse(null);
                if (status == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hệ thống chưa cấu hình trạng thái giỏ hàng (ID=3)");

                cart.setUserSC(user);
                cart.setCartStatus(status);
                cart = shopcartRepo.save(cart);
            }

            // 2. Tìm kiếm item khớp hoàn toàn mọi thuộc tính (Biến thể)
            Optional<CartDetailEntity> existingDetail = cartDetailRepo.findByAllAttributes(
                    cart.getId(), productId, colorId, materialId, dimensionId);

            if (existingDetail.isPresent()) {
                CartDetailEntity detail = existingDetail.get();
                detail.setAmountCD(detail.getAmountCD() + amount);
                detail.setUpdatedAt(new Date());
                cartDetailRepo.save(detail);
            } else {
                CartDetailEntity detail = new CartDetailEntity();
                detail.setShopcartdetail(cart);
                
                ProductEntity product = productRepo.findById(productId).orElse(null);
                if (product == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm");
                
                detail.setProductcartdetail(product);
                detail.setAmountCD(amount);
                detail.setCreatedAt(new Date());
                detail.setUpdatedAt(new Date());
                
                if (colorId != null) colorRepo.findById(colorId).ifPresent(detail::setColorEntity);
                if (materialId != null) materialRepo.findById(materialId).ifPresent(detail::setMaterialcartdetail);
                if (dimensionId != null) dimensionsRepo.findById(dimensionId).ifPresent(detail::setDemensionsCartDetail);
                
                cartDetailRepo.save(detail);
            }

            return ResponseEntity.ok("Đã thêm vào giỏ hàng");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove-item")
    public ResponseEntity<?> removeItem(@RequestParam Long idCartDetail) {
        try {
            cartDetailRepo.deleteById(idCartDetail);
            return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            java.util.List<ShopcartEntity> carts = shopcartRepo.findByUserSC_IdAndCartStatus_Id(userId, 3L);
            for (ShopcartEntity cart : carts) {
                // Xóa tất cả chi tiết dựa trên ID giỏ hàng
                java.util.List<CartDetailEntity> items = cartDetailRepo.findAll();
                for(CartDetailEntity it : items) {
                    if(it.getShopcartdetail().getId().equals(cart.getId())) {
                        cartDetailRepo.delete(it);
                    }
                }
            }
            return ResponseEntity.ok("Đã dọn dẹp giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/update-amount")
    public ResponseEntity<?> updateAmount(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Long idCartDetail = Long.valueOf(payload.get("idCartDetail").toString());
            Long amount = Long.valueOf(payload.get("amount").toString());
            
            Optional<CartDetailEntity> detailOpt = cartDetailRepo.findById(idCartDetail);
            if (detailOpt.isPresent()) {
                CartDetailEntity detail = detailOpt.get();
                if (amount <= 0) {
                    cartDetailRepo.delete(detail);
                    return ResponseEntity.ok("Đã xóa sản phẩm");
                }
                detail.setAmountCD(amount);
                detail.setUpdatedAt(new Date());
                cartDetailRepo.save(detail);
                return ResponseEntity.ok("Đã cập nhật số lượng");
            }
            return ResponseEntity.status(404).body("Không tìm thấy mục giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
}
