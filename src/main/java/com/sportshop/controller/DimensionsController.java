package com.sportshop.controller;

import com.sportshop.dto.DimensionsDTO;
import com.sportshop.entity.DimensionsEntity;
import com.sportshop.entity.ProductEntity;
import com.sportshop.repository.DimensionsRepository;
import com.sportshop.repository.ProductRepository;
import com.sportshop.response.PageResponse;
import com.sportshop.service.IDimensionsService;
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
@RequestMapping("/admin/product-dimensions")
public class DimensionsController {

    @Autowired
    private IDimensionsService dimensionsService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DimensionsRepository dimensionsRepository;

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
                    sortObj = Sort.by(Sort.Direction.ASC, "created_at");
                    break;
                case "az":
                    sortObj = Sort.by(Sort.Direction.ASC, "nameD");
                    break;
                case "za":
                    sortObj = Sort.by(Sort.Direction.DESC, "nameD");
                    break;
                case "newest":
                default:
                    sortObj = Sort.by(Sort.Direction.DESC, "created_at");
                    break;
            }

            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<DimensionsDTO> result = dimensionsService.findAll(
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            dimensionsService.delete(id);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody DimensionsDTO dto) {
        try {
            dimensionsService.save(dto);
            return ResponseEntity.ok("Thêm mới thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DimensionsDTO dto) {
        try {
            dimensionsService.update(id, dto);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] excelData = dimensionsService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "product_dimensions.xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/preview-import")
    public ResponseEntity<?> previewImport(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<Map<String, Object>> previewList = new ArrayList<>();

            // Find column indices
            int sizeCol = -1, productCol = -1;
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String head = formatter.formatCellValue(headerRow.getCell(c)).trim().toLowerCase();
                    if (head.contains("kích cỡ") || head.contains("size") || head.contains("dimension")) sizeCol = c;
                    if (head.contains("sản phẩm") || head.contains("product")) productCol = c;
                }
            }
            if (sizeCol == -1) sizeCol = 1;
            if (productCol == -1) productCol = 2;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String sizeName = formatter.formatCellValue(row.getCell(sizeCol)).trim();
                String productName = formatter.formatCellValue(row.getCell(productCol)).trim();

                if (sizeName.isEmpty() || productName.isEmpty()) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("nameD", sizeName);
                item.put("productName", productName);

                Optional<ProductEntity> productOpt = productRepository.findFirstByName(productName);

                if (productOpt.isPresent()) {
                    item.put("productId", productOpt.get().getId());
                    item.put("exists", dimensionsRepository.existsByNameDAndDemensions_Id(sizeName, productOpt.get().getId()));
                    item.put("isValid", true);
                } else {
                    item.put("isValid", false);
                    item.put("errors", "Không tìm thấy sản phẩm");
                }

                previewList.add(item);
            }
            return ResponseEntity.ok(previewList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi đọc file: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-import")
    public ResponseEntity<?> confirmImport(@RequestBody List<Map<String, Object>> items) {
        try {
            int count = 0;
            for (Map<String, Object> mapData : items) {
                Boolean isValid = (Boolean) mapData.get("isValid");
                Boolean exists = (Boolean) mapData.get("exists");
                if (isValid != null && isValid && (exists == null || !exists)) {
                    String nameD = (String) mapData.get("nameD");
                    Long productId = ((Number) mapData.get("productId")).longValue();
                    
                    DimensionsEntity entity = new DimensionsEntity();
                    entity.setNameD(nameD);
                    entity.setDemensions(productRepository.findById(productId).orElse(null));
                    
                    dimensionsRepository.save(entity);
                    count++;
                }
            }
            return ResponseEntity.ok("Đã nhập thành công " + count + " kích cỡ sản phẩm!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }
}
