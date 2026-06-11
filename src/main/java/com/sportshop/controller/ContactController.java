package com.sportshop.controller;

import com.sportshop.entity.ContactEntity;
import com.sportshop.service.IContactService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private IContactService contactService;

    @GetMapping("/list")
    public ResponseEntity<?> listContacts(
            @RequestParam(value = "search", required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        try {
            Page<ContactEntity> page;
            if (search != null && !search.isEmpty()) {
                page = contactService.search(search, pageable);
            } else {
                page = contactService.findAll(pageable);
            }
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi tải danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addContact(@RequestBody ContactEntity contact) {
        try {
            ContactEntity savedContact = contactService.save(contact);
            return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi gửi liên hệ: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            ContactEntity updated = contactService.updateStatus(id, status);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi cập nhật trạng thái: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=contacts.xlsx");

        java.util.List<ContactEntity> contacts = contactService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Contacts");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"STT", "Họ và tên", "Email", "Số điện thoại", "Chủ đề", "Nội dung", "Ngày tạo", "Trạng thái"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        dateStyle.setDataFormat(df.getFormat("dd/MM/yyyy HH:mm:ss"));
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);

        int rowCount = 1;
        for (int i = 0; i < contacts.size(); i++) {
            ContactEntity c = contacts.get(i);
            Row row = sheet.createRow(rowCount++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(i + 1);
            cell0.setCellStyle(normalStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(c.getFullName() != null ? c.getFullName() : "");
            cell1.setCellStyle(normalStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(c.getEmail() != null ? c.getEmail() : "");
            cell2.setCellStyle(normalStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(c.getPhoneNumber() != null ? c.getPhoneNumber() : "");
            cell3.setCellStyle(normalStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(c.getSubject() != null ? c.getSubject() : "");
            cell4.setCellStyle(normalStyle);

            Cell cell5 = row.createCell(5);
            cell5.setCellValue(c.getMessage() != null ? c.getMessage() : "");
            cell5.setCellStyle(normalStyle);

            Cell cell6 = row.createCell(6);
            if (c.getCreatedAt() != null) {
                cell6.setCellValue(c.getCreatedAt());
                cell6.setCellStyle(dateStyle);
            } else {
                cell6.setCellValue("");
                cell6.setCellStyle(normalStyle);
            }

            Cell cell7 = row.createCell(7);
            String statusStr = (c.getStatus() != null && c.getStatus() == 1) ? "Đã xử lý" : "Chưa xử lý";
            cell7.setCellValue(statusStr);
            cell7.setCellStyle(normalStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        try {
            contactService.delete(id);
            return new ResponseEntity<>("Đã xóa liên hệ thành công", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi xóa: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
