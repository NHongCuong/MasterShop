package com.sportshop.service.impl;

import com.sportshop.converter.MaterialConverter;
import com.sportshop.dto.MaterialDTO;
import com.sportshop.entity.MaterialEntity;
import com.sportshop.repository.MaterialRepository;
import com.sportshop.service.IMaterialService;
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
public class MaterialService implements IMaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialConverter materialConverter;

    @Override
    public List<MaterialDTO> findAllDTO() {
        List<MaterialEntity> entities = materialRepository.findAll();
        List<MaterialDTO> result = new ArrayList<>();
        for (MaterialEntity e : entities) {
            result.add(materialConverter.toDTO(e));
        }
        return result;
    }

    @Override
    public Page<MaterialDTO> findAll(String search, Pageable pageable) {
        Page<MaterialEntity> page = materialRepository.findWithSearch(search, pageable);
        return page.map(materialConverter::toDTO);
    }

    @Override
    public void delete(Long id) {
        materialRepository.deleteById(id);
    }

    @Override
    public void save(MaterialDTO dto) {
        if (materialRepository.existsByNameMaterial(dto.getNameMaterial())) {
            throw new RuntimeException("Tên nguyên liệu đã tồn tại");
        }
        MaterialEntity entity = materialConverter.toEntity(dto);
        materialRepository.save(entity);
    }

    @Override
    public void update(Long id, MaterialDTO dto) {
        MaterialEntity oldEntity = materialRepository.findById(id).orElse(null);
        if (oldEntity != null) {
            if (!oldEntity.getNameMaterial().equals(dto.getNameMaterial())) {
                if (materialRepository.existsByNameMaterial(dto.getNameMaterial())) {
                    throw new RuntimeException("Tên nguyên liệu đã tồn tại");
                }
            }
            MaterialEntity entity = materialConverter.toEntity(dto);
            entity.setId(id);
            entity.setCreatedAt(oldEntity.getCreatedAt());
            entity.setUpdatedAt(new Date());
            materialRepository.save(entity);
        }
    }

    @Override
    public byte[] exportToExcel() throws Exception {
        List<MaterialDTO> allData = findAllDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Materials");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
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
            String[] headers = {"STT", "ID", "Tên nguyên liệu", "Ngày tạo", "Ngày sửa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (MaterialDTO dto : allData) {
                Row row = sheet.createRow(rowNum);
                Cell c0 = row.createCell(0); c0.setCellValue(rowNum); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(dto.getId()); c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(dto.getNameMaterial() != null ? dto.getNameMaterial() : ""); c2.setCellStyle(dataStyle);
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
