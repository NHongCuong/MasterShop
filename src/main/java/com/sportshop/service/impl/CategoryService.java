package com.sportshop.service.impl;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.sportshop.converter.CategoryConverter;
import com.sportshop.dto.CategoryDTO;
import com.sportshop.entity.CategoryEntity;
import com.sportshop.repository.CategoryRepository;
import com.sportshop.service.ICategoryService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService implements ICategoryService {
	@Autowired
	CategoryRepository categoryRepo;
	@Autowired
	CategoryConverter categoryConverter;

	@Override
	public List<CategoryDTO> getAll() {
		List<CategoryEntity> list = categoryRepo.findAll();
		List<CategoryDTO> result = new ArrayList<>();
		for (CategoryEntity en : list) {
			result.add(categoryConverter.toDTO(en));
		}
		return result;
	}

	@Override
	public Page<CategoryDTO> findAll(String search, Pageable pageable) {
		Page<CategoryEntity> page = categoryRepo.findWithSearch(search, pageable);
		return page.map(categoryConverter::toDTO);
	}

	@Override
	public CategoryDTO get(Long id) {
		CategoryEntity category = categoryRepo.findById(id).orElse(null);
		return categoryConverter.toDTO(category);
	}

	@Override
	public void save(CategoryDTO dto) {
		if (categoryRepo.findFirstByName(dto.getName()).isPresent()) {
			throw new RuntimeException("Tên danh mục đã tồn tại");
		}
		CategoryEntity entity = categoryConverter.toEntity(dto);
		categoryRepo.save(entity);
	}

	@Override
	public void update(Long id, CategoryDTO dto) {
		CategoryEntity oldEntity = categoryRepo.findById(id).orElse(null);
		if (oldEntity != null) {
			if (!oldEntity.getName().equals(dto.getName())) {
				if (categoryRepo.findFirstByName(dto.getName()).isPresent()) {
					throw new RuntimeException("Tên danh mục đã tồn tại");
				}
			}
			oldEntity.setName(dto.getName());
			oldEntity.setIcon(dto.getIcon());
			oldEntity.setUpdatedAt(new Date());
			categoryRepo.save(oldEntity);
		}
	}

	@Override
	public void delete(Long id) {
		categoryRepo.deleteById(id);
	}

	@Override
	public byte[] exportToExcel() throws Exception {
		List<CategoryDTO> allData = getAll();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Categories");

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
			String[] headers = {"STT", "Tên danh mục", "Ảnh", "Ngày tạo", "Ngày sửa"};
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
			}

			int rowNum = 1;
			for (CategoryDTO dto : allData) {
				Row row = sheet.createRow(rowNum);
				Cell c0 = row.createCell(0); c0.setCellValue(rowNum); c0.setCellStyle(dataStyle);
				Cell c1 = row.createCell(1); c1.setCellValue(dto.getName() != null ? dto.getName() : ""); c1.setCellStyle(dataStyle);
				Cell c2 = row.createCell(2); c2.setCellValue(dto.getIcon() != null ? dto.getIcon() : ""); c2.setCellStyle(dataStyle);
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

				String name = getCellValueAsString(row.getCell(0)); // Column A: Tên danh mục
				String icon = getCellValueAsString(row.getCell(1)); // Column B: Ảnh
				if (name.isEmpty()) continue;

				if (categoryRepo.findFirstByName(name).isEmpty()) {
					CategoryEntity entity = new CategoryEntity();
					entity.setName(name);
					entity.setIcon(icon);
					categoryRepo.save(entity);
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
