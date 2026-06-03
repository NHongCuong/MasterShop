package com.sportshop.controller;

import com.sportshop.entity.CategoryEntity;
import com.sportshop.entity.ProductImageEntity;
import com.sportshop.entity.ProductEntity;
import com.sportshop.entity.SupplierEntity;
import com.sportshop.repository.CategoryRepository;
import com.sportshop.repository.ProductRepository;
import com.sportshop.repository.SupplierRepository;
import com.sportshop.service.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/product")
@CrossOrigin
public class ProductController {
    @Autowired
    ProductService productService;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private SupplierRepository supplierRepo;

    // 🟢 API xem danh sách sản phẩm
    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts() {
        try {
            return new ResponseEntity<>(productService.getAllProduct(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductEntity> getProductById(@PathVariable("id") Long id) {
        ProductEntity productEntity = productService.getByID(id);
        return new ResponseEntity<>(productEntity, HttpStatus.OK);
    }

    // 🟢 API thêm sản phẩm
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ProductEntity product) {
        try {
            // Gán category (nếu có ID)
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                CategoryEntity cate = categoryRepo.findById(product.getCategory().getId()).orElse(null);
                product.setCategory(cate);
            } else {
                // Nếu không có category, có thể gán null hoặc category mặc định
                product.setCategory(null);
            }

            // Gán supplier (nếu có ID)
            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                SupplierEntity sup = supplierRepo.findById(product.getSupplier().getId()).orElse(null);
                product.setSupplier(sup);
            } else {
                product.setSupplier(null);
            }

            // Gán các ảnh phụ
            if (product.getProductImages() != null) {
                product.getProductImages().forEach(img -> img.setProduct(product));
            }

            // Lưu sản phẩm
            ProductEntity saved = productRepo.save(product);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm sản phẩm: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 🟡 API sửa sản phẩm
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductEntity product) {
        try {
            ProductEntity existing = productService.getByID(id);
            if (existing == null) {
                return new ResponseEntity<>("Không tìm thấy sản phẩm ID " + id, HttpStatus.NOT_FOUND);
            }

            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setPrice(product.getPrice());
            existing.setAvatar(product.getAvatar());
            existing.setAmount(product.getAmount());

            if (product.getCategory() != null && product.getCategory().getId() != null) {
                CategoryEntity cate = categoryRepo.findById(product.getCategory().getId()).orElse(null);
                existing.setCategory(cate);
            }

            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                SupplierEntity sup = supplierRepo.findById(product.getSupplier().getId()).orElse(null);
                existing.setSupplier(sup);
            }

            // Cập nhật các ảnh phụ
            if (product.getProductImages() != null) {
                existing.getProductImages().clear();
                for (ProductImageEntity img : product.getProductImages()) {
                    img.setProduct(existing);
                    existing.getProductImages().add(img);
                }
            }

            ProductEntity updated = productService.save(existing);
            return new ResponseEntity<>(updated, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi cập nhật: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 🔴 API xóa sản phẩm
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productRepo.deleteById(id);
            return new ResponseEntity<>("Đã xóa sản phẩm ID " + id, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi xóa: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
