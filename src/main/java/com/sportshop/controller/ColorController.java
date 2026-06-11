package com.sportshop.controller;

import com.sportshop.entity.ColorEntity;
import com.sportshop.repository.ColorRepository;
import com.sportshop.service.IColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sportshop.dto.ColorDTO;
import com.sportshop.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/color")
public class ColorController {
    @Autowired
    private IColorService colorService;

    @Autowired
    private ColorRepository colorRepository;

    @GetMapping("/all-public")
    public ResponseEntity<?> getAllPublic() {
        return ResponseEntity.ok(colorService.findAllDTO());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        try {
            Sort sortObj;
            switch (sort) {
                case "oldest": sortObj = Sort.by(Sort.Direction.ASC, "createdAt"); break;
                case "az": sortObj = Sort.by(Sort.Direction.ASC, "nameColor"); break;
                case "za": sortObj = Sort.by(Sort.Direction.DESC, "nameColor"); break;
                case "newest":
                default: sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); break;
            }
            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<ColorDTO> result = colorService.findAll(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    pageable
            );
            return ResponseEntity.ok(PageResponse.of(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách: " + e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> getList() {
        return ResponseEntity.ok(colorService.findAllDTO());
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody ColorDTO dto) {
        try {
            colorService.save(dto);
            return ResponseEntity.ok("Thêm mới thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ColorDTO dto) {
        try {
            colorService.update(id, dto);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            colorService.delete(id);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] data = colorService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "colors.xlsx");
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
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

            // Dynamic header search
            int nameColIndex = -1;
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String head = formatter.formatCellValue(headerRow.getCell(c)).trim().toLowerCase();
                    if (head.contains("tên màu") || head.contains("name") || head.contains("color")) {
                        nameColIndex = c;
                        break;
                    }
                }
            }
            
            // Fallback to index 2 if not found (matching system export)
            if (nameColIndex == -1) nameColIndex = 2;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nameColor = formatter.formatCellValue(row.getCell(nameColIndex)).trim();
                if (nameColor.isEmpty() && nameColIndex != 0) {
                    // Try index 0 if index 2 is empty and it wasn't index 0 already
                    nameColor = formatter.formatCellValue(row.getCell(0)).trim();
                }
                
                if (nameColor.isEmpty()) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("nameColor", nameColor);
                item.put("exists", colorRepository.existsByNameColor(nameColor));
                previewList.add(item);
            }
            return ResponseEntity.ok(previewList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi đọc file: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-import")
    public ResponseEntity<?> confirmImport(@RequestBody List<Map<String, Object>> colors) {
        try {
            int count = 0;
            for (Map<String, Object> colorData : colors) {
                String name = (String) colorData.get("nameColor");
                if (name == null || name.isEmpty()) continue;

                if (!colorRepository.existsByNameColor(name)) {
                    ColorEntity entity = new ColorEntity();
                    entity.setNameColor(name);
                    colorRepository.save(entity);
                    count++;
                }
            }
            return ResponseEntity.ok("Đã nhập thành công " + count + " màu sắc mới!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }
}
