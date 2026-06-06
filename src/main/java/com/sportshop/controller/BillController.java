package com.sportshop.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import com.sportshop.entity.BillEntity;
import com.sportshop.repository.BillRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/bill")
public class BillController {

    @Autowired
    private BillRepository billRepo;

    @GetMapping("/list")
    public Page<BillEntity> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "id,desc") String sort) {
        
        String[] sortParts = sort.split(",");
        Sort sortObj = Sort.by(sortParts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        if (search.isEmpty()) {
            return billRepo.findAll(pageable);
        } else {
            return billRepo.findBySearch(search, pageable);
        }
    }

    @GetMapping("/order/{orderId}")
    public List<BillEntity> getByOrderId(@PathVariable Long orderId) {
        return billRepo.findByOrderbill_Id(orderId);
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=bills.xlsx");

        List<BillEntity> list = billRepo.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bills");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID Bill", "ID Order", "Khách hàng", "Ngày tạo", "Tổng tiền gốc", "Giảm giá", "Thanh toán"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        currencyStyle.setDataFormat(df.getFormat("#,##0"));
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);

        int rowCount = 1;
        for (BillEntity b : list) {
            Row row = sheet.createRow(rowCount++);
            Cell c0 = row.createCell(0); c0.setCellValue(b.getId()); c0.setCellStyle(normalStyle);
            Cell c1 = row.createCell(1); c1.setCellValue(b.getOrderbill() != null ? b.getOrderbill().getId() : 0); c1.setCellStyle(normalStyle);
            Cell c2 = row.createCell(2); c2.setCellValue(b.getOrderbill() != null ? b.getOrderbill().getCustomerName() : "N/A"); c2.setCellStyle(normalStyle);
            Cell c3 = row.createCell(3); c3.setCellValue(b.getCreateDate() != null ? b.getCreateDate().toString() : "N/A"); c3.setCellStyle(normalStyle);
            Cell c4 = row.createCell(4); c4.setCellValue(b.getTotalMoney() != null ? b.getTotalMoney() : 0); c4.setCellStyle(currencyStyle);
            Cell c5 = row.createCell(5); c5.setCellValue(b.getDiscount() != null ? b.getDiscount() : 0); c5.setCellStyle(currencyStyle);
            Cell c6 = row.createCell(6); c6.setCellValue(b.getTotalMoneyaftersaleoff() != null ? b.getTotalMoneyaftersaleoff() : 0); c6.setCellStyle(currencyStyle);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
