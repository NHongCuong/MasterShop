package com.sportshop.service.impl;

import com.sportshop.converter.DimensionsConverter;
import com.sportshop.dto.DimensionsDTO;
import com.sportshop.entity.DimensionsEntity;
import com.sportshop.repository.DimensionsRepository;
import com.sportshop.service.IDimensionsService;
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
public class DimensionsService implements IDimensionsService {

    @Autowired
    private DimensionsRepository dimensionsRepository;

    @Autowired
    private DimensionsConverter dimensionsConverter;

    @Override
    public List<DimensionsDTO> findAllDTO() {
        List<DimensionsEntity> entities = dimensionsRepository.findAll();
        List<DimensionsDTO> result = new ArrayList<>();
        for (DimensionsEntity e : entities) {
            result.add(dimensionsConverter.toDTO(e));
        }
        return result;
    }

    @Override
    public List<DimensionsDTO> findByProductId(Long productId) {
        return dimensionsRepository.findByDemensions_Id(productId).stream()
                .map(dimensionsConverter::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public DimensionsDTO get(Long id) {
        DimensionsEntity entity = dimensionsRepository.findById(id).orElse(null);
        return dimensionsConverter.toDTO(entity);
    }

    @Override
    public Page<DimensionsDTO> findAll(String search, Pageable pageable) {
        Page<DimensionsEntity> page = dimensionsRepository.findWithSearch(search, pageable);
        return page.map(dimensionsConverter::toDTO);
    }

    @Override
    public void delete(Long id) {
        dimensionsRepository.deleteById(id);
    }

    @Override
    public void save(DimensionsDTO dto) {
        if (dimensionsRepository.existsByNameDAndDemensions_Id(dto.getNameD(), dto.getProductId())) {
            throw new RuntimeException("Sản phẩm đã tồn tại kích cỡ này");
        }
        DimensionsEntity entity = dimensionsConverter.toEntity(dto);
        dimensionsRepository.save(entity);
    }

    @Override
    public void update(Long id, DimensionsDTO dto) {
        DimensionsEntity oldEntity = dimensionsRepository.findById(id).orElse(null);
        if (oldEntity != null) {
            // Nếu đổi tên hoặc đổi sản phẩm, kiểm tra trùng lặp
            if (!oldEntity.getNameD().equals(dto.getNameD()) || !oldEntity.getDemensions().getId().equals(dto.getProductId())) {
                if (dimensionsRepository.existsByNameDAndDemensions_Id(dto.getNameD(), dto.getProductId())) {
                    throw new RuntimeException("Sản phẩm đã tồn tại kích cỡ này");
                }
            }
            DimensionsEntity entity = dimensionsConverter.toEntity(dto);
            entity.setId(id);
            entity.setCreated_at(oldEntity.getCreated_at());
            entity.setUpdated_at(new Date());
            dimensionsRepository.save(entity);
        }
    }

    @Override
    public byte[] exportToExcel() throws Exception {
        List<DimensionsDTO> allData = findAllDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product Dimensions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
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
            String[] headers = {"STT", "Tên kích cỡ", "Tên sản phẩm", "Ngày tạo", "Ngày sửa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (DimensionsDTO dto : allData) {
                Row row = sheet.createRow(rowNum);
                Cell c0 = row.createCell(0); c0.setCellValue(rowNum); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(dto.getNameD() != null ? dto.getNameD() : ""); c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(dto.getProductName() != null ? dto.getProductName() : ""); c2.setCellStyle(dataStyle);
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
