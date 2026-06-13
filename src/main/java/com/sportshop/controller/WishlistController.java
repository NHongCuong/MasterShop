package com.sportshop.controller;

import com.sportshop.entity.ProductEntity;
import com.sportshop.entity.UserEntity;
import com.sportshop.entity.WishlistEntity;
import com.sportshop.repository.ProductRepository;
import com.sportshop.repository.UserRepository;
import com.sportshop.repository.WishlistRepository;
import com.sportshop.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/wishlist")
@CrossOrigin
public class WishlistController {
    @Autowired
    private WishlistRepository wishlistRepo;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private ProductRepository productRepo;

    @PostMapping("/toggle")
    @Transactional
    public ResponseEntity<?> toggleWishlist(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Long productId = Long.valueOf(payload.get("productId").toString());

            Optional<WishlistEntity> existing = wishlistRepo.findByUserIdAndProductId(userId, productId);
            if (existing.isPresent()) {
                wishlistRepo.deleteByUserIdAndProductId(userId, productId);
                return ResponseEntity.ok(Map.of("status", "removed"));
            } else {
                UserEntity user = userRepo.findById(userId).orElseThrow();
                ProductEntity product = productRepo.findById(productId).orElseThrow();
                WishlistEntity entity = new WishlistEntity();
                entity.setUser(user);
                entity.setProduct(product);
                wishlistRepo.save(entity);
                return ResponseEntity.ok(Map.of("status", "added"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserWishlist(@PathVariable Long userId) {
        return ResponseEntity.ok(wishlistRepo.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<?> checkWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        boolean exists = wishlistRepo.existsByUserIdAndProductId(userId, productId);
        return ResponseEntity.ok(Map.of("isWishlisted", exists));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<PageResponse<WishlistEntity>> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<WishlistEntity> result = wishlistRepo.findWithSearch(search, PageRequest.of(page, size, Sort.by("id").descending()));
        return ResponseEntity.ok(PageResponse.of(result));
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteWishlist(@PathVariable Long id) {
        wishlistRepo.deleteById(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=wishlists.xlsx";
        response.setHeader(headerKey, headerValue);

        List<WishlistEntity> list = wishlistRepo.findAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Wishlists");
            Row headerRow = sheet.createRow(0);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"STT", "Tên người dùng", "Tên sản phẩm", "Ngày tạo", "Ngày sửa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowCount = 1;
            for (WishlistEntity item : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(rowCount);
                row.createCell(1).setCellValue(item.getUser() != null ? item.getUser().getNameUser() : "");
                row.createCell(2).setCellValue(item.getProduct() != null ? item.getProduct().getName() : "");
                row.createCell(3).setCellValue(item.getCreatedAt() != null ? sdf.format(item.getCreatedAt()) : "");
                row.createCell(4).setCellValue(item.getUpdatedAt() != null ? sdf.format(item.getUpdatedAt()) : "---");
                rowCount++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
}
