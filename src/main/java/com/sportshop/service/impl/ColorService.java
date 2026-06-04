package com.sportshop.service.impl;

import com.sportshop.converter.ColorConverter;
import com.sportshop.dto.ColorDTO;
import com.sportshop.entity.ColorEntity;
import com.sportshop.repository.ColorRepository;
import com.sportshop.service.IColorService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ColorService implements IColorService {

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ColorConverter colorConverter;

    @Override
    public List<ColorDTO> findAllDTO() {
        List<ColorEntity> entities = colorRepository.findAll();
        List<ColorDTO> result = new ArrayList<>();
        for (ColorEntity e : entities) {
            result.add(colorConverter.toDTO(e));
        }
        return result;
    }

    @Override
    public Page<ColorDTO> findAll(String search, Pageable pageable) {
        Page<ColorEntity> page = colorRepository.findWithSearch(search, pageable);
        return page.map(colorConverter::toDTO);
    }

    @Override
    public void delete(Long id) {
        colorRepository.deleteById(id);
    }

    @Override
    public void save(ColorDTO dto) {
        if (colorRepository.existsByNameColor(dto.getNameColor())) {
            throw new RuntimeException("Tên màu đã tồn tại");
        }
        ColorEntity entity = colorConverter.toEntity(dto);
        colorRepository.save(entity);
    }

    @Override
    public void update(Long id, ColorDTO dto) {
        ColorEntity oldEntity = colorRepository.findById(id).orElse(null);
        if (oldEntity != null) {
            if (!oldEntity.getNameColor().equals(dto.getNameColor())) {
                if (colorRepository.existsByNameColor(dto.getNameColor())) {
                    throw new RuntimeException("Tên màu đã tồn tại");
                }
            }
            ColorEntity entity = colorConverter.toEntity(dto);
            entity.setId(id);
            entity.setCreatedAt(oldEntity.getCreatedAt());
            entity.setUpdatedAt(new Date());
            colorRepository.save(entity);
        }
    }

    @Override
    public byte[] exportToExcel() throws Exception {
        List<ColorDTO> allData = findAllDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Colors");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "ID", "Tên màu", "Ngày tạo", "Ngày sửa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (ColorDTO dto : allData) {
                Row row = sheet.createRow(rowNum);
                Cell c0 = row.createCell(0); c0.setCellValue(rowNum); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(dto.getId()); c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(dto.getNameColor() != null ? dto.getNameColor() : ""); c2.setCellStyle(dataStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(dto.getCreatedAt() != null ? sdf.format(dto.getCreatedAt()) : "---"); c3.setCellStyle(dataStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(dto.getUpdatedAt() != null ? sdf.format(dto.getUpdatedAt()) : "---"); c4.setCellStyle(dataStyle);
                rowNum++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
