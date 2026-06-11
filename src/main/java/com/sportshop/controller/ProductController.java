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
import com.sportshop.service.ProductInventoryService;
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
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private VoucherRepository voucherRepo;

    @Autowired
    private ProductInventoryService productInventoryService;

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
            else if (sort.equals("za")) sortObj = Sort.by("name").descending();

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

    @GetMapping("/{id}")
    public ResponseEntity<ProductEntity> getProductById(@PathVariable("id") Long id) {
        ProductEntity productEntity = productService.getByID(id);
        return new ResponseEntity<>(productEntity, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ProductEntity product) {
        try {
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                CategoryEntity cate = categoryRepo.findById(product.getCategory().getId()).orElse(null);
                product.setCategory(cate);
            }

            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                SupplierEntity sup = supplierRepo.findById(product.getSupplier().getId()).orElse(null);
                product.setSupplier(sup);
            }

            if (product.getVoucher() != null && product.getVoucher().getId() != null) {
                VoucherEntity vch = voucherRepo.findById(product.getVoucher().getId()).orElse(null);
                product.setVoucher(vch);
            }

            if (product.getProductImages() != null) {
                product.getProductImages().forEach(img -> img.setProduct(product));
            }

            ProductEntity saved = productRepo.save(product);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi thêm sản phẩm: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
            existing.setWarranty(product.getWarranty());

            if (product.getCategory() != null && product.getCategory().getId() != null) {
                existing.setCategory(categoryRepo.findById(product.getCategory().getId()).orElse(null));
            }

            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                existing.setSupplier(supplierRepo.findById(product.getSupplier().getId()).orElse(null));
            }

            if (product.getVoucher() != null && product.getVoucher().getId() != null) {
                existing.setVoucher(voucherRepo.findById(product.getVoucher().getId()).orElse(null));
            } else {
                existing.setVoucher(null);
            }

            if (product.getProductImages() != null) {
                existing.getProductImages().clear();
                for (ProductImageEntity img : product.getProductImages()) {
                    img.setProduct(existing);
                    existing.getProductImages().add(img);
                }
            }

            existing.setUpdatedAt(new Date());
            ProductEntity updated = productService.save(existing);
            return new ResponseEntity<>(updated, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi cập nhật: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inventory")
    public ResponseEntity<PageResponse<ProductEntity>> getInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return ResponseEntity.ok(productInventoryService.getInventory(page, size, sort, search, lowStockOnly));
    }

    @GetMapping("/inventory/low-stock-count")
    public ResponseEntity<Map<String, Long>> getLowStockCount() {
        return ResponseEntity.ok(Map.of("count", productInventoryService.countLowStock()));
    }

    @PutMapping("/stock/{id}")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            if (body.get("amount") == null) {
                return ResponseEntity.badRequest().body("Thiếu số lượng tồn kho");
            }
            long amount = Long.parseLong(body.get("amount").toString());
            ProductEntity updated = productInventoryService.updateStock(id, amount);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi cập nhật tồn kho: " + e.getMessage());
        }
    }

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"STT", "Tên sản phẩm", "Ảnh", "Mô tả", "Giá", "Giảm giá (%)", "Tồn kho", "Đã bán", "Danh mục", "Nhà cung cấp", "Voucher", "Bảo hành", "Ngày tạo", "Ngày sửa"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowCount = 1;
        for (ProductEntity prod : listProducts) {
            Row row = sheet.createRow(rowCount);
            row.createCell(0).setCellValue(rowCount);
            row.createCell(1).setCellValue(prod.getName());
            row.createCell(2).setCellValue(prod.getAvatar() != null ? prod.getAvatar() : "");
            row.createCell(3).setCellValue(prod.getDescription());
            row.createCell(4).setCellValue(prod.getPrice() != null ? prod.getPrice() : 0);
            row.createCell(5).setCellValue(prod.getDiscountPercent() != null ? prod.getDiscountPercent() : 0);
            long stockIn = parseStockAmount(prod.getAmount());
            long sold = prod.getSoldQuantity() != null ? prod.getSoldQuantity() : 0;
            row.createCell(6).setCellValue(stockIn);
            row.createCell(7).setCellValue(sold);
            row.createCell(8).setCellValue(prod.getCategory() != null ? prod.getCategory().getName() : "");
            row.createCell(9).setCellValue(prod.getSupplier() != null ? prod.getSupplier().getName() : "");
            row.createCell(10).setCellValue(prod.getVoucher() != null ? prod.getVoucher().getMaVoucher() : "");
            row.createCell(11).setCellValue(prod.getWarranty() != null ? prod.getWarranty() : "");
            row.createCell(12).setCellValue(prod.getCreatedAt() != null ? sdf.format(prod.getCreatedAt()) : "");
            row.createCell(13).setCellValue(prod.getUpdatedAt() != null ? sdf.format(prod.getUpdatedAt()) : "");
            rowCount++;
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/preview-import")
    public ResponseEntity<?> previewImport(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<Map<String, Object>> previewList = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> item = new HashMap<>();
                // Row mapping adjusted for STT column at index 0
                item.put("name", formatter.formatCellValue(row.getCell(1)));
                item.put("avatar", formatter.formatCellValue(row.getCell(2)));
                item.put("description", formatter.formatCellValue(row.getCell(3)));
                
                String priceStr = formatter.formatCellValue(row.getCell(4)).replaceAll("[^\\d.]", "");
                item.put("price", priceStr.isEmpty() ? 0L : (long)Double.parseDouble(priceStr));
                
                String discountStr = formatter.formatCellValue(row.getCell(5)).replaceAll("[^\\d.]", "");
                item.put("discountPercent", discountStr.isEmpty() ? 0 : (int)Double.parseDouble(discountStr));
                
                item.put("amount", formatter.formatCellValue(row.getCell(6)));
                
                String soldStr = formatter.formatCellValue(row.getCell(7)).replaceAll("[^\\d.]", "");
                item.put("soldQuantity", soldStr.isEmpty() ? 0L : (long)Double.parseDouble(soldStr));

                String categoryName = formatter.formatCellValue(row.getCell(8));
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("name", categoryName);
                if (!categoryName.isEmpty()) {
                    Optional<CategoryEntity> cat = categoryRepo.findByName(categoryName);
                    if (cat.isPresent()) categoryMap.put("id", cat.get().getId());
                }
                item.put("category", categoryMap);

                String supplierName = formatter.formatCellValue(row.getCell(9));
                Map<String, Object> supplierMap = new HashMap<>();
                supplierMap.put("name", supplierName);
                if (!supplierName.isEmpty()) {
                    Optional<SupplierEntity> sup = supplierRepo.findByName(supplierName);
                    if (sup.isPresent()) supplierMap.put("id", sup.get().getId());
                }
                item.put("supplier", supplierMap);

                String voucherName = formatter.formatCellValue(row.getCell(10));
                Map<String, Object> voucherMap = new HashMap<>();
                voucherMap.put("name", voucherName);
                if (!voucherName.isEmpty()) {
                    Optional<VoucherEntity> vch = voucherRepo.findByMaVoucher(voucherName);
                    if (vch.isPresent()) voucherMap.put("id", vch.get().getId());
                }
                item.put("voucher", voucherMap);

                item.put("warranty", formatter.formatCellValue(row.getCell(11)));

                previewList.add(item);
            }
            workbook.close();
            return ResponseEntity.ok(previewList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi preview: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/confirm-import")
    public ResponseEntity<?> confirmImport(@RequestBody List<Map<String, Object>> products) {
        try {
            int count = 0;
            for (Map<String, Object> prodData : products) {
                ProductEntity product = new ProductEntity();
                product.setName((String) prodData.get("name"));
                product.setAvatar((String) prodData.get("avatar"));
                product.setDescription((String) prodData.get("description"));
                product.setPrice(((Number) prodData.get("price")).longValue());
                product.setDiscountPercent(((Number) prodData.get("discountPercent")).intValue());
                product.setAmount(prodData.get("amount").toString());
                product.setSoldQuantity(((Number) prodData.get("soldQuantity")).longValue());
                product.setWarranty(prodData.get("warranty") != null ? prodData.get("warranty").toString() : "");

                Map<String, Object> catMap = (Map<String, Object>) prodData.get("category");
                if (catMap != null && catMap.get("name") != null) {
                    product.setCategory(categoryRepo.findByName((String) catMap.get("name")).orElse(null));
                }
                Map<String, Object> supMap = (Map<String, Object>) prodData.get("supplier");
                if (supMap != null && supMap.get("name") != null) {
                    product.setSupplier(supplierRepo.findByName((String) supMap.get("name")).orElse(null));
                }
                Map<String, Object> vchMap = (Map<String, Object>) prodData.get("voucher");
                if (vchMap != null && vchMap.get("name") != null) {
                    product.setVoucher(voucherRepo.findByMaVoucher((String) vchMap.get("name")).orElse(null));
                }

                productRepo.save(product);
                count++;
            }
            return ResponseEntity.ok("Nhập thành công " + count + " sản phẩm!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
        }
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
