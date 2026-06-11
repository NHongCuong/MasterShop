package com.sportshop.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import com.sportshop.entity.BillEntity;
import com.sportshop.repository.BillRepository;
import com.sportshop.entity.OderDetailEntity;
import com.sportshop.repository.OderDetailRepository;
import com.sportshop.response.PageResponse;
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

    @Autowired
    private OderDetailRepository orderDetailRepo;

    @GetMapping("/list")
    public PageResponse<BillEntity> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "id,desc") String sort) {
        
        String[] sortParts = sort.split(",");
        Sort sortObj = Sort.by(sortParts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        if (search.isEmpty()) {
            return PageResponse.of(billRepo.findAll(pageable));
        } else {
            return PageResponse.of(billRepo.findBySearch(search, pageable));
        }
    }

    @GetMapping("/my-bills")
    public PageResponse<BillEntity> myBills(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createDate,desc") String sort) {
        
        String[] sortParts = sort.split(",");
        Sort sortObj = Sort.by(sortParts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        return PageResponse.of(billRepo.findByEmail(email, pageable));
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
        String[] headers = {
            "ID Hóa đơn", "ID Đơn", "Khách hàng", "Số điện thoại", "Email", "Địa chỉ giao hàng",
            "Người nhận khác", "SĐT Người nhận", "Sản phẩm & Thuộc tính", "Số lượng", "Đơn giá", "Thành tiền",
            "Tổng tiền hàng", "Giảm giá hóa đơn", "Tổng thanh toán",
            "PT Thanh toán", "Vận chuyển", "Ngày tạo"
        };
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

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(df.getFormat("dd/MM/yyyy HH:mm:ss"));
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);

        int rowCount = 1;
        for (BillEntity b : list) {
            java.util.List<OderDetailEntity> items = (b.getOrderbill() != null) 
                ? orderDetailRepo.findByOrderId(b.getOrderbill().getId()) 
                : new java.util.ArrayList<>();
            
            if (items.isEmpty()) {
                Row row = sheet.createRow(rowCount++);
                writeBillRow(row, b, null, normalStyle, currencyStyle, dateStyle);
            } else {
                for (OderDetailEntity item : items) {
                    Row row = sheet.createRow(rowCount++);
                    writeBillRow(row, b, item, normalStyle, currencyStyle, dateStyle);
                }
            }
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void writeBillRow(Row row, BillEntity b, OderDetailEntity item, CellStyle normal, CellStyle currency, CellStyle date) {
        com.sportshop.entity.OderEntity order = b.getOrderbill();
        
        row.createCell(0).setCellValue(b.getId());
        row.createCell(1).setCellValue(order != null ? order.getId() : 0);
        row.createCell(2).setCellValue(order != null ? order.getCustomerName() : "N/A");
        row.createCell(3).setCellValue(order != null ? order.getPhone() : "");
        row.createCell(4).setCellValue(order != null ? order.getEmail() : "");
        row.createCell(5).setCellValue(order != null ? order.getAddressO() : "");
        row.createCell(6).setCellValue(order != null ? order.getReceiverName() : "");
        row.createCell(7).setCellValue(order != null ? order.getReceiverPhone() : "");
        
        if (item != null) {
            StringBuilder sb = new StringBuilder(item.getProduct().getName());
            if (item.getColor() != null || item.getMaterial() != null || item.getDimensions() != null) {
                sb.append(" (");
                boolean f = true;
                if (item.getColor() != null) { sb.append("Màu: ").append(item.getColor().getNameColor()); f = false; }
                if (item.getMaterial() != null) { if(!f) sb.append(", "); sb.append("Chất liệu: ").append(item.getMaterial().getNameMaterial()); f = false; }
                if (item.getDimensions() != null) { if(!f) sb.append(", "); sb.append("Kích cỡ: ").append(item.getDimensions().getNameD()); }
                sb.append(")");
            }
            row.createCell(8).setCellValue(sb.toString());
            row.createCell(9).setCellValue(item.getAmount());
            row.createCell(10).setCellValue(item.getPrice());
            row.createCell(11).setCellValue(item.getAmount() * item.getPrice());
        } else {
            row.createCell(8).setCellValue("");
            row.createCell(9).setCellValue(0);
            row.createCell(10).setCellValue(0);
            row.createCell(11).setCellValue(0);
        }

        row.createCell(12).setCellValue(b.getTotalMoney() != null ? b.getTotalMoney() : 0);
        row.createCell(13).setCellValue(b.getDiscount() != null ? b.getDiscount() : 0);
        row.createCell(14).setCellValue(b.getTotalMoneyaftersaleoff() != null ? b.getTotalMoneyaftersaleoff() : 0);
        
        row.createCell(15).setCellValue(order != null && order.getMethodofPayment() != null ? order.getMethodofPayment().getName_mop() : "N/A");
        row.createCell(16).setCellValue(order != null && order.getShipMethod() != null ? order.getShipMethod().getNameSM() : "N/A");
        
        Cell c17 = row.createCell(17);
        if (b.getCreateDate() != null) {
            c17.setCellValue(b.getCreateDate());
            c17.setCellStyle(date);
        } else {
            c17.setCellValue("N/A");
            c17.setCellStyle(normal);
        }

        // Apply styles to all cells
        for (int i = 0; i <= 17; i++) {
            Cell c = row.getCell(i);
            if (c == null) c = row.createCell(i);
            if (i == 10 || i == 11 || i == 12 || i == 13 || i == 14) {
                c.setCellStyle(currency);
            } else if (i != 17) {
                c.setCellStyle(normal);
            }
        }
    }
}
