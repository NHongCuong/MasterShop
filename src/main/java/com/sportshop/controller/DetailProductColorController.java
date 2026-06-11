package com.sportshop.controller;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.entity.ColorEntity;
import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.DetailProductColorId;
import com.sportshop.entity.ProductEntity;
import com.sportshop.repository.ColorRepository;
import com.sportshop.repository.DetailProductColorRepository;
import com.sportshop.repository.ProductRepository;
import com.sportshop.response.PageResponse;
import com.sportshop.service.IDetailProductColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/admin/product-color")
public class DetailProductColorController {

    @Autowired
    private IDetailProductColorService detailProductColorService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private DetailProductColorRepository detailProductColorRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        try {
            Sort sortObj;
            switch (sort) {
                case "oldest":
                    sortObj = Sort.by(Sort.Direction.ASC, "createdAt");
                    break;
                case "az":
                    sortObj = Sort.by(Sort.Direction.ASC, "detailColor.nameColor");
                    break;
                case "za":
                    sortObj = Sort.by(Sort.Direction.DESC, "detailColor.nameColor");
                    break;
                case "newest":
                default:
                    sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }

            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<DetailProductColorDTO> result = detailProductColorService.findAll(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    pageable
            );
            return ResponseEntity.ok(PageResponse.of(result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long idColor, @RequestParam Long idProduct) {
        try {
            detailProductColorService.delete(idColor, idProduct);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody DetailProductColorDTO dto) {
        try {
            detailProductColorService.save(dto);
            return ResponseEntity.ok("Thêm mới thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestParam Long oldIdColor, @RequestParam Long oldIdProduct,
                                   @RequestBody DetailProductColorDTO dto) {
        try {
            detailProductColorService.update(oldIdColor, oldIdProduct, dto);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] excelData = detailProductColorService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "product_colors.xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/preview-import")
    public ResponseEntity<?> previewImport(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<Map<String, Object>> previewList = new ArrayList<>();

            // Find column indices for Color Name and Product Name
            int colorCol = -1, productCol = -1;
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String head = formatter.formatCellValue(headerRow.getCell(c)).trim().toLowerCase();
                    if (head.contains("màu") || head.contains("color")) colorCol = c;
                    if (head.contains("sản phẩm") || head.contains("product")) productCol = c;
                }
            }
            // Fallbacks based on default export (STT, Color, Product...)
            if (colorCol == -1) colorCol = 1;
            if (productCol == -1) productCol = 2;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String colorName = formatter.formatCellValue(row.getCell(colorCol)).trim();
                String productName = formatter.formatCellValue(row.getCell(productCol)).trim();

                if (colorName.isEmpty() || productName.isEmpty()) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("colorName", colorName);
                item.put("productName", productName);

                // Find entities to validate and get IDs
                Optional<ColorEntity> colorOpt = colorRepository.findByNameColor(colorName);
                Optional<ProductEntity> productOpt = productRepository.findFirstByName(productName);

                if (colorOpt.isPresent() && productOpt.isPresent()) {
                    item.put("idColor", colorOpt.get().getId());
                    item.put("idProduct", productOpt.get().getId());
                    
                    // Check if mapping already exists
                    DetailProductColorId id = new DetailProductColorId(colorOpt.get().getId(), productOpt.get().getId());
                    item.put("exists", detailProductColorRepository.existsById(id));
                    item.put("isValid", true);
                } else {
                    item.put("isValid", false);
                    List<String> errors = new ArrayList<>();
                    if (colorOpt.isEmpty()) errors.add("Không tìm thấy màu");
                    if (productOpt.isEmpty()) errors.add("Không tìm thấy sản phẩm");
                    item.put("errors", String.join(", ", errors));
                }

                previewList.add(item);
            }
            return ResponseEntity.ok(previewList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi đọc file: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-import")
    public ResponseEntity<?> confirmImport(@RequestBody List<Map<String, Object>> mappings) {
        try {
            int count = 0;
            for (Map<String, Object> mapData : mappings) {
                Boolean isValid = (Boolean) mapData.get("isValid");
                Boolean exists = (Boolean) mapData.get("exists");
                if (isValid != null && isValid && (exists == null || !exists)) {
                    Long idColor = ((Number) mapData.get("idColor")).longValue();
                    Long idProduct = ((Number) mapData.get("idProduct")).longValue();
                    
                    DetailProductColorEntity entity = new DetailProductColorEntity();
                    DetailProductColorId id = new DetailProductColorId(idColor, idProduct);
                    entity.setId(id);
                    
                    // Need to set entities for JPA mapping
                    entity.setDetailColor(colorRepository.findById(idColor).orElse(null));
                    entity.setDetailColorProduct(productRepository.findById(idProduct).orElse(null));
                    
                    detailProductColorRepository.save(entity);
                    count++;
                }
            }
            return ResponseEntity.ok("Đã nhập thành công " + count + " mối liên kết sản phẩm - màu sắc!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }
}
