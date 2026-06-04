package com.sportshop.service.impl;

import com.sportshop.converter.DetailProductMaterialConverter;
import com.sportshop.dto.DetailProductMaterialDTO;
import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.entity.DetailProductMaterialId;
import com.sportshop.repository.DetailProductMaterialRepository;
import com.sportshop.service.IDetailProductMaterialService;
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
public class DetailProductMaterialService implements IDetailProductMaterialService {

    @Autowired
    private DetailProductMaterialRepository detailProductMaterialRepository;

    @Autowired
    private DetailProductMaterialConverter detailProductMaterialConverter;

    @Override
    public List<DetailProductMaterialDTO> findAllDTO() {
        List<DetailProductMaterialEntity> entities = detailProductMaterialRepository.findAll();
        List<DetailProductMaterialDTO> result = new ArrayList<>();
        for (DetailProductMaterialEntity e : entities) {
            result.add(detailProductMaterialConverter.toDTO(e));
        }
        return result;
    }

    @Override
    public DetailProductMaterialDTO get(Long idMaterial, Long idProduct) {
        DetailProductMaterialId id = new DetailProductMaterialId(idMaterial, idProduct);
        DetailProductMaterialEntity entity = detailProductMaterialRepository.findById(id).orElse(null);
        return detailProductMaterialConverter.toDTO(entity);
    }

    @Override
    public Page<DetailProductMaterialDTO> findAll(String search, Pageable pageable) {
        Page<DetailProductMaterialEntity> page = detailProductMaterialRepository.findWithSearch(search, pageable);
        return page.map(detailProductMaterialConverter::toDTO);
    }

    @Override
    public void delete(Long idMaterial, Long idProduct) {
        DetailProductMaterialId id = new DetailProductMaterialId(idMaterial, idProduct);
        detailProductMaterialRepository.deleteById(id);
    }

    @Override
    public void save(DetailProductMaterialDTO dto) {
        DetailProductMaterialId id = new DetailProductMaterialId(dto.getIdMaterial(), dto.getIdProduct());
        if (detailProductMaterialRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm tương ứng với nguyên liệu đã tồn tại");
        }
        DetailProductMaterialEntity entity = detailProductMaterialConverter.toEntity(dto);
        detailProductMaterialRepository.save(entity);
    }

    @Override
    public void update(Long oldIdMaterial, Long oldIdProduct, DetailProductMaterialDTO dto) {
        DetailProductMaterialId oldId = new DetailProductMaterialId(oldIdMaterial, oldIdProduct);
        DetailProductMaterialId newId = new DetailProductMaterialId(dto.getIdMaterial(), dto.getIdProduct());
        
        // Nếu ID thay đổi, kiểm tra xem ID mới đã tồn tại chưa
        if (!oldId.equals(newId)) {
            if (detailProductMaterialRepository.existsById(newId)) {
                throw new RuntimeException("Sản phẩm tương ứng với nguyên liệu đã tồn tại");
            }
            detailProductMaterialRepository.deleteById(oldId);
        }
        
        DetailProductMaterialEntity oldEntity = detailProductMaterialRepository.findById(oldId).orElse(null);
        Date originalCreated = (oldEntity != null) ? oldEntity.getCreatedAt() : new Date();

        DetailProductMaterialEntity entity = detailProductMaterialConverter.toEntity(dto);
        entity.setCreatedAt(originalCreated);
        entity.setUpdatedAt(new Date());
        detailProductMaterialRepository.save(entity);
    }

    @Override
    public byte[] exportToExcel() throws Exception {
        List<DetailProductMaterialDTO> allData = findAllDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product Materials");

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

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Tên nguyên liệu", "Tên sản phẩm", "Ngày tạo", "Ngày sửa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (DetailProductMaterialDTO dto : allData) {
                Row row = sheet.createRow(rowNum);
                Cell c0 = row.createCell(0); c0.setCellValue(rowNum); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(dto.getNameMaterial() != null ? dto.getNameMaterial() : ""); c1.setCellStyle(dataStyle);
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
