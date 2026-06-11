package com.sportshop.controller;

import com.sportshop.dto.VoucherDTO;
import com.sportshop.entity.VoucherEntity;
import com.sportshop.repository.VoucherRepository;
import com.sportshop.response.PageResponse;
import com.sportshop.service.IVoucherService;
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

@CrossOrigin
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    IVoucherService voucherService;

    @Autowired
    VoucherRepository voucherRepository;

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
                case "az":     sortObj = Sort.by(Sort.Direction.ASC, "maVoucher"); break;
                case "za":     sortObj = Sort.by(Sort.Direction.DESC, "maVoucher"); break;
                default:       sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
            }
            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<VoucherDTO> result = voucherService.findAll(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    pageable);
            return ResponseEntity.ok(PageResponse.of(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getList() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    @GetMapping("/check/{code}")
    public ResponseEntity<?> checkVoucher(@PathVariable String code) {
        VoucherDTO dto = voucherService.getByCode(code);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mã voucher không hợp lệ");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        VoucherDTO dto = voucherService.get(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody VoucherDTO dto) {
        try {
            voucherService.save(dto);
            return ResponseEntity.ok("Thêm voucher thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody VoucherDTO dto) {
        try {
            voucherService.update(id, dto);
            return ResponseEntity.ok("Cập nhật voucher thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            voucherService.delete(id);
            return ResponseEntity.ok("Đã xóa voucher ID " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] data = voucherService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "vouchers.xlsx");
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

            // Find column indices
            int codeCol = -1, discountCol = -1;
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String head = formatter.formatCellValue(headerRow.getCell(c)).trim().toLowerCase();
                    if (head.contains("mã") || head.contains("voucher") || head.contains("code")) codeCol = c;
                    if (head.contains("giảm giá") || head.contains("discount") || head.contains("%")) discountCol = c;
                }
            }
            if (codeCol == -1) codeCol = 1;
            if (discountCol == -1) discountCol = 2;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String code = formatter.formatCellValue(row.getCell(codeCol)).trim();
                String discountStr = (discountCol != -1) ? formatter.formatCellValue(row.getCell(discountCol)).trim() : "0";

                if (code.isEmpty()) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("maVoucher", code);
                
                try {
                    // Remove % sign if exists and convert to int
                    String cleanDiscount = discountStr.replace("%", "").trim();
                    item.put("discountPercent", Integer.parseInt(cleanDiscount));
                } catch (Exception e) {
                    item.put("discountPercent", 0);
                }

                item.put("exists", voucherRepository.existsByMaVoucher(code));
                item.put("isValid", true);

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
                    String code = (String) mapData.get("maVoucher");
                    Integer discount = (Integer) mapData.get("discountPercent");

                    VoucherEntity entity = new VoucherEntity();
                    entity.setMaVoucher(code);
                    entity.setDiscountPercent(discount);
                    voucherRepository.save(entity);
                    count++;
                }
            }
            return ResponseEntity.ok("Đã nhập thành công " + count + " voucher!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }
}
