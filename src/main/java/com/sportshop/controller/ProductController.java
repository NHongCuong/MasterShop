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
import com.sportshop.response.PageResponse;
import com.sportshop.service.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


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
            return ResponseEntity.ok(PageResponse.of(result));
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
            existing.setDiscountPercent(product.getDiscountPercent());

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

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=products.xlsx";
        response.setHeader(headerKey, headerValue);

        List<ProductEntity> listProducts = productRepo.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Tên sản phẩm", "Mô tả", "Giá", "Giảm giá (%)", "Tồn kho", "Đã bán", "Danh mục", "Nhà cung cấp"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowCount = 1;
        for (ProductEntity prod : listProducts) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(prod.getId());
            row.createCell(1).setCellValue(prod.getName());
            row.createCell(2).setCellValue(prod.getDescription());
            row.createCell(3).setCellValue(prod.getPrice() != null ? prod.getPrice() : 0);
            row.createCell(4).setCellValue(prod.getDiscountPercent() != null ? prod.getDiscountPercent() : 0);
            long stockIn = parseStockAmount(prod.getAmount());
            long sold = prod.getSoldQuantity() != null ? prod.getSoldQuantity() : 0;
            row.createCell(5).setCellValue(stockIn);
            row.createCell(6).setCellValue(sold);
            row.createCell(7).setCellValue(prod.getCategory() != null ? prod.getCategory().getName() : "");
            row.createCell(8).setCellValue(prod.getSupplier() != null ? prod.getSupplier().getName() : "");
        }

        // Auto size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private long parseStockAmount(String amount) {
        if (amount == null || amount.isBlank()) return 0;
        try {
            return Long.parseLong(amount.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
