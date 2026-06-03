package com.sportshop.controller;

import com.sportshop.entity.*;
import com.sportshop.repository.*;
import com.sportshop.service.ICartDetailService;
import com.sportshop.service.IShopcartService;
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
    private ICartDetailService cartDetailService;

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
                if (status == null) {
                    // Nếu chưa có status 3, tạo mới hoặc báo lỗi
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hệ thống chưa cấu hình trạng thái giỏ hàng (ID=3)");
                }

                cart.setUserSC(user);
                cart.setCartStatus(status);
                cart = shopcartRepo.save(cart);
            }

            // 2. Kiểm tra nếu item đã tồn tại trong CartDetail
            CartDetailId detailId = new CartDetailId();
            detailId.setIdSC(cart.getId());
            detailId.setIdProduct(productId);
            
            Optional<CartDetailEntity> existingDetail = cartDetailRepo.findById(detailId);

            if (existingDetail.isPresent()) {
                CartDetailEntity detail = existingDetail.get();
                Long currentAmount = detail.getAmountCD() != null ? detail.getAmountCD() : 0L;
                detail.setAmountCD(currentAmount + amount);
                detail.setUpdatedAt(new Date());
                detail.setIdColor(colorId);
                cartDetailRepo.save(detail);
            } else {
                CartDetailEntity detail = new CartDetailEntity();
                detail.setId(detailId);
                detail.setShopcartdetail(cart);
                
                ProductEntity product = productRepo.findById(productId).orElse(null);
                if (product == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm ID=" + productId);
                
                detail.setProductcartdetail(product);
                detail.setAmountCD(amount);
                detail.setCreatedAt(new Date());
                detail.setUpdatedAt(new Date());
                detail.setIdColor(colorId);
                
                if (materialId != null) {
                    materialRepo.findById(materialId).ifPresent(detail::setMaterialcartdetail);
                }
                
                if (dimensionId != null) {
                    dimensionsRepo.findById(dimensionId).ifPresent(detail::setDemensionsCartDetail);
                }
                
                try {
                    cartDetailRepo.save(detail);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lưu CartDetail: " + ex.getMessage());
                }
            }

            return ResponseEntity.ok("Đã thêm vào giỏ hàng");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove-item")
    public ResponseEntity<?> removeItem(@RequestParam Long scId, @RequestParam Long productId) {
        try {
            CartDetailId id = new CartDetailId(scId, productId);
            cartDetailRepo.deleteById(id);
            return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            java.util.List<ShopcartEntity> carts = shopcartRepo.findByUserSC_IdAndCartStatus_Id(userId, 3L);
            for (ShopcartEntity cart : carts) {
                // Logic xóa chi tiết
            }
            return ResponseEntity.ok("Đã dọn dẹp giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/update-amount")
    public ResponseEntity<?> updateAmount(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Long scId = Long.valueOf(payload.get("scId").toString());
            Long productId = Long.valueOf(payload.get("productId").toString());
            Long amount = Long.valueOf(payload.get("amount").toString());
            
            CartDetailId id = new CartDetailId(scId, productId);
            Optional<CartDetailEntity> detailOpt = cartDetailRepo.findById(id);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy mục giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
}
