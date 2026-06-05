package com.sportshop.controller;

import com.sportshop.entity.CategoryEntity;
import com.sportshop.entity.ProductImageEntity;
import com.sportshop.entity.ProductEntity;
import com.sportshop.entity.SupplierEntity;
import com.sportshop.entity.VoucherEntity;
import com.sportshop.repository.CategoryRepository;
import com.sportshop.repository.ProductRepository;
import com.sportshop.repository.SupplierRepository;
import com.sportshop.repository.VoucherRepository;
import com.sportshop.service.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @Autowired
    private VoucherRepository voucherRepo;

    // 🟢 API xem danh sách sản phẩm
    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts() {
        try {
            return new ResponseEntity<>(productService.getAllProduct(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all-paged")
    public ResponseEntity<?> getPagedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(required = false) String search) {
        try {
            Sort sortObj = Sort.by("id").descending();
            if (sort.equals("price_asc")) sortObj = Sort.by("price").ascending();
            else if (sort.equals("price_desc")) sortObj = Sort.by("price").descending();
            else if (sort.equals("az")) sortObj = Sort.by("name").ascending();

            Page<ProductEntity> result;
            if (search != null && !search.isEmpty()) {
                result = productRepo.findByNameContainingIgnoreCase(search, PageRequest.of(page, size, sortObj));
            } else {
                result = productRepo.findAll(PageRequest.of(page, size, sortObj));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi phân trang: " + e.getMessage());
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

            // Gán voucher (nếu có)
            if (product.getVoucher() != null && product.getVoucher().getId() != null) {
                VoucherEntity vch = voucherRepo.findById(product.getVoucher().getId()).orElse(null);
                product.setVoucher(vch);
            } else {
                product.setVoucher(null);
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

            if (product.getVoucher() != null && product.getVoucher().getId() != null) {
                VoucherEntity vch = voucherRepo.findById(product.getVoucher().getId()).orElse(null);
                existing.setVoucher(vch);
            } else {
                existing.setVoucher(null);
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
