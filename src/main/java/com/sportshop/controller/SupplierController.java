package com.sportshop.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import com.sportshop.entity.SupplierEntity;
import com.sportshop.repository.SupplierRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/supplier")
public class SupplierController {
    @Autowired
    private SupplierRepository supplierRepo;

    @GetMapping("/list")
    public Page<SupplierEntity> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "id,desc") String sort) {
        
        String[] sortParts = sort.split(",");
        Sort sortObj = Sort.by(sortParts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        if (search.isEmpty()) {
            return supplierRepo.findAll(pageable);
        } else {
            return supplierRepo.findBySearch(search, pageable);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody SupplierEntity supplier) {
        try {
            Long maxId = supplierRepo.findAll().stream().mapToLong(s -> s.getId()).max().orElse(0L);
            supplier.setId(maxId + 1);
            supplierRepo.save(supplier);
            return ResponseEntity.ok("Thêm nhà cung cấp thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SupplierEntity supplier) {
        try {
            SupplierEntity exist = supplierRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
            exist.setName(supplier.getName());
            exist.setAddress(supplier.getAddress());
            exist.setPhone(supplier.getPhone());
            exist.setEmail(supplier.getEmail());
            exist.setWebsite(supplier.getWebsite());
            supplierRepo.save(exist);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            supplierRepo.deleteById(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: Không thể xóa nhà cung cấp này vì đang có sản phẩm liên kết.");
        }
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=suppliers.xlsx");

        List<SupplierEntity> list = supplierRepo.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Suppliers");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Tên nhà cung cấp", "Địa chỉ", "Số điện thoại", "Email", "Website"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);

        int rowCount = 1;
        for (SupplierEntity s : list) {
            Row row = sheet.createRow(rowCount++);
            Cell c0 = row.createCell(0); c0.setCellValue(s.getId()); c0.setCellStyle(borderStyle);
            Cell c1 = row.createCell(1); c1.setCellValue(s.getName()); c1.setCellStyle(borderStyle);
            Cell c2 = row.createCell(2); c2.setCellValue(s.getAddress()); c2.setCellStyle(borderStyle);
            Cell c3 = row.createCell(3); c3.setCellValue(s.getPhone()); c3.setCellStyle(borderStyle);
            Cell c4 = row.createCell(4); c4.setCellValue(s.getEmail()); c4.setCellStyle(borderStyle);
            Cell c5 = row.createCell(5); c5.setCellValue(s.getWebsite()); c5.setCellStyle(borderStyle);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
