package com.sportshop.controller;

import com.sportshop.dto.DetailProductMaterialDTO;
import com.sportshop.entity.MaterialEntity;
import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.entity.DetailProductMaterialId;
import com.sportshop.entity.ProductEntity;
import com.sportshop.repository.MaterialRepository;
import com.sportshop.repository.DetailProductMaterialRepository;
import com.sportshop.repository.ProductRepository;
import com.sportshop.response.PageResponse;
import com.sportshop.service.IDetailProductMaterialService;
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
@RequestMapping("/admin/product-material")
public class DetailProductMaterialController {

    @Autowired
    private IDetailProductMaterialService detailProductMaterialService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private DetailProductMaterialRepository detailProductMaterialRepository;

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
                    sortObj = Sort.by(Sort.Direction.ASC, "detailMaterial.nameMaterial");
                    break;
                case "za":
                    sortObj = Sort.by(Sort.Direction.DESC, "detailMaterial.nameMaterial");
                    break;
                case "newest":
                default:
                    sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }

            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<DetailProductMaterialDTO> result = detailProductMaterialService.findAll(
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
    public ResponseEntity<?> delete(@RequestParam Long idMaterial, @RequestParam Long idProduct) {
        try {
            detailProductMaterialService.delete(idMaterial, idProduct);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody DetailProductMaterialDTO dto) {
        try {
            detailProductMaterialService.save(dto);
            return ResponseEntity.ok("Thêm mới thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm: " + e.getMessage());
        }
    }

    @PostMapping("/preview-import")
    public ResponseEntity<?> previewImport(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<Map<String, Object>> previewList = new ArrayList<>();

            // Find column indices
            int materialCol = -1, productCol = -1;
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String head = formatter.formatCellValue(headerRow.getCell(c)).trim().toLowerCase();
                    if (head.contains("nguyên liệu") || head.contains("chất liệu") || head.contains("material")) materialCol = c;
                    if (head.contains("sản phẩm") || head.contains("product")) productCol = c;
                }
            }
            if (materialCol == -1) materialCol = 1;
            if (productCol == -1) productCol = 2;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String materialName = formatter.formatCellValue(row.getCell(materialCol)).trim();
                String productName = formatter.formatCellValue(row.getCell(productCol)).trim();

                if (materialName.isEmpty() || productName.isEmpty()) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("materialName", materialName);
                item.put("productName", productName);

                Optional<MaterialEntity> materialOpt = materialRepository.findFirstByNameMaterial(materialName);
                Optional<ProductEntity> productOpt = productRepository.findFirstByName(productName);

                if (materialOpt.isPresent() && productOpt.isPresent()) {
                    item.put("idMaterial", materialOpt.get().getId());
                    item.put("idProduct", productOpt.get().getId());
                    
                    DetailProductMaterialId id = new DetailProductMaterialId(materialOpt.get().getId(), productOpt.get().getId());
                    item.put("exists", detailProductMaterialRepository.existsById(id));
                    item.put("isValid", true);
                } else {
                    item.put("isValid", false);
                    List<String> errors = new ArrayList<>();
                    if (materialOpt.isEmpty()) errors.add("Không tìm thấy nguyên liệu");
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
                    Long idMaterial = ((Number) mapData.get("idMaterial")).longValue();
                    Long idProduct = ((Number) mapData.get("idProduct")).longValue();
                    
                    DetailProductMaterialEntity entity = new DetailProductMaterialEntity();
                    DetailProductMaterialId id = new DetailProductMaterialId(idMaterial, idProduct);
                    entity.setId(id);
                    
                    entity.setDetailMaterial(materialRepository.findById(idMaterial).orElse(null));
                    entity.setDetailMaterialProduct(productRepository.findById(idProduct).orElse(null));
                    
                    detailProductMaterialRepository.save(entity);
                    count++;
                }
            }
            return ResponseEntity.ok("Đã nhập thành công " + count + " mối liên kết sản phẩm - nguyên liệu!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestParam Long oldIdMaterial, @RequestParam Long oldIdProduct,
                                   @RequestBody DetailProductMaterialDTO dto) {
        try {
            detailProductMaterialService.update(oldIdMaterial, oldIdProduct, dto);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] excelData = detailProductMaterialService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "product_materials.xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
