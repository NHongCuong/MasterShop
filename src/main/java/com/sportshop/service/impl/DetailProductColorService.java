package com.sportshop.service.impl;

import com.sportshop.converter.DetailProductColorConverter;
import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.DetailProductColorId;
import com.sportshop.repository.DetailProductColorRepository;
import com.sportshop.service.IDetailProductColorService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class DetailProductColorService implements IDetailProductColorService {

    @Autowired
    private DetailProductColorRepository detailProductColorRepository;

    @Autowired
    private DetailProductColorConverter detailProductColorConverter;

    @Override
    public List<DetailProductColorDTO> findAllDTO() {
        List<DetailProductColorEntity> entities = detailProductColorRepository.findAll();
        List<DetailProductColorDTO> result = new ArrayList<>();
        for (DetailProductColorEntity e : entities) {
            if (e == null) continue;
            result.add(detailProductColorConverter.toDTO(e));
        }
        return result;
    }

    @Override
    public DetailProductColorDTO get(Long idColor, Long idProduct) {
        DetailProductColorId id = new DetailProductColorId(idColor, idProduct);
        DetailProductColorEntity entity = detailProductColorRepository.findById(id).orElse(null);
        if (entity == null) return null;
        return detailProductColorConverter.toDTO(entity);
    }

    @Override
    public Page<DetailProductColorDTO> findAll(String search, Pageable pageable) {
        Page<DetailProductColorEntity> page = detailProductColorRepository.findWithSearch(search, pageable);
        return page.map(detailProductColorConverter::toDTO);
    }

    @Override
    public void delete(Long idColor, Long idProduct) {
        DetailProductColorId id = new DetailProductColorId(idColor, idProduct);
        detailProductColorRepository.deleteById(id);
    }

    @Override
    public void save(DetailProductColorDTO dto) {
        DetailProductColorId id = new DetailProductColorId(dto.getIdColor(), dto.getIdProduct());
        if (detailProductColorRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm tương ứng với màu đã tồn tại");
        }
        DetailProductColorEntity entity = detailProductColorConverter.toEntity(dto);
        detailProductColorRepository.save(entity);
    }

    @Override
    public void update(Long oldIdColor, Long oldIdProduct, DetailProductColorDTO dto) {
        DetailProductColorId oldId = new DetailProductColorId(oldIdColor, oldIdProduct);
        DetailProductColorId newId = new DetailProductColorId(dto.getIdColor(), dto.getIdProduct());
        
        // Nếu ID thay đổi, kiểm tra xem ID mới đã tồn tại chưa
        if (!oldId.equals(newId)) {
            if (detailProductColorRepository.existsById(newId)) {
                throw new RuntimeException("Sản phẩm tương ứng với màu đã tồn tại");
            }
            detailProductColorRepository.deleteById(oldId);
        }
        
        DetailProductColorEntity oldEntity = detailProductColorRepository.findById(oldId).orElse(null);
        java.util.Date originalCreated = (oldEntity != null) ? oldEntity.getCreatedAt() : new java.util.Date();

        DetailProductColorEntity entity = detailProductColorConverter.toEntity(dto);
        
        // Giữ nguyên ngày tạo và cập nhật ngày sửa
        entity.setCreatedAt(originalCreated);
        entity.setUpdatedAt(new java.util.Date());
        
        detailProductColorRepository.save(entity);
    }

    @Override
    public byte[] exportToExcel() throws Exception {
        List<DetailProductColorDTO> allData = findAllDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product Colors");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Tên màu", "Tên sản phẩm", "Ngày tạo", "Ngày sửa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (DetailProductColorDTO dto : allData) {
                Row row = sheet.createRow(rowNum);

                Cell c0 = row.createCell(0); c0.setCellValue(rowNum); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(dto.getNameColor() != null ? dto.getNameColor() : ""); c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(dto.getProductName() != null ? dto.getProductName() : ""); c2.setCellStyle(dataStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(dto.getCreatedAt() != null ? sdf.format(dto.getCreatedAt()) : "---"); c3.setCellStyle(dataStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(dto.getUpdatedAt() != null ? sdf.format(dto.getUpdatedAt()) : "---"); c4.setCellStyle(dataStyle);

                rowNum++;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
