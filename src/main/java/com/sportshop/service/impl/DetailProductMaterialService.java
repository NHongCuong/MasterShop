package com.sportshop.service.impl;

import com.sportshop.converter.DetailProductMaterialConverter;
import com.sportshop.dto.DetailProductMaterialDTO;
import com.sportshop.entity.DetailProductMaterialEntity;
import com.sportshop.entity.DetailProductMaterialId;
import com.sportshop.repository.DetailProductMaterialRepository;
import com.sportshop.repository.MaterialRepository;
import com.sportshop.repository.ProductRepository;
import com.sportshop.service.IDetailProductMaterialService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.sportshop.entity.MaterialEntity;
import com.sportshop.entity.ProductEntity;

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
    private MaterialRepository materialRepository;

    @Autowired
    private ProductRepository productRepository;

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
        
        // Luôn lấy entity cũ trước để giữ lại Ngày tạo
        DetailProductMaterialEntity oldEntity = detailProductMaterialRepository.findById(oldId).orElse(null);
        Date originalCreated = (oldEntity != null) ? oldEntity.getCreatedAt() : new Date();

        // Nếu ID thay đổi, kiểm tra xem ID mới đã tồn tại chưa
        if (!oldId.equals(newId)) {
            if (detailProductMaterialRepository.existsById(newId)) {
                throw new RuntimeException("Sản phẩm tương ứng với nguyên liệu đã tồn tại");
            }
            detailProductMaterialRepository.deleteById(oldId);
        }
        
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

    @Override
    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String materialName = getCellValueAsString(row.getCell(0));
                String productName = getCellValueAsString(row.getCell(1));

                if (materialName.isEmpty() || productName.isEmpty()) continue;

                MaterialEntity material = materialRepository.findFirstByNameMaterial(materialName)
                        .orElseThrow(() -> new RuntimeException("Nguyên liệu '" + materialName + "' không tồn tại"));
                ProductEntity product = productRepository.findFirstByName(productName)
                        .orElseThrow(() -> new RuntimeException("Sản phẩm '" + productName + "' không tồn tại"));

                DetailProductMaterialId id = new DetailProductMaterialId(material.getId(), product.getId());
                if (!detailProductMaterialRepository.existsById(id)) {
                    DetailProductMaterialEntity entity = new DetailProductMaterialEntity();
                    entity.setId(id);
                    entity.setDetailMaterial(material);
                    entity.setDetailMaterialProduct(product);
                    entity.setCreatedAt(new Date());
                    detailProductMaterialRepository.save(entity);
                }
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }
}
