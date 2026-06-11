package com.sportshop.controller;

import com.sportshop.dto.CategoryDTO;
import com.sportshop.entity.CategoryEntity;
import com.sportshop.repository.CategoryRepository;
import com.sportshop.response.PageResponse;
import com.sportshop.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/category")
public class CategoryController {
	@Autowired
	ICategoryService categoryService;

	@Autowired
	CategoryRepository categoryRepository;

	@GetMapping("/all")
	public ResponseEntity<?> getAll(
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "newest") String sort) {
		try {
			Sort sortObj;
			switch (sort) {
				case "oldest":
					sortObj = Sort.by(Sort.Direction.ASC, "createdAt");
					break;
				case "az":
					sortObj = Sort.by(Sort.Direction.ASC, "name");
					break;
				case "za":
					sortObj = Sort.by(Sort.Direction.DESC, "name");
					break;
				case "newest":
				default:
					sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
					break;
			}
			Pageable pageable = PageRequest.of(page, size, sortObj);
			Page<CategoryDTO> result = categoryService.findAll(
					(search != null && !search.trim().isEmpty()) ? search.trim() : null,
					pageable);
			return ResponseEntity.ok(PageResponse.of(result));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Lỗi khi lấy danh sách: " + e.getMessage());
		}
	}

	@GetMapping("/list")
	public ResponseEntity<?> getList() {
		return ResponseEntity.ok(categoryService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getCategoryById(@PathVariable("id") Long id) {
		CategoryDTO category = categoryService.get(id);
		if (category == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(category);
	}

	@PostMapping("/add")
	public ResponseEntity<?> addCategory(@RequestBody CategoryDTO dto) {
		try {
			categoryService.save(dto);
			return ResponseEntity.ok("Thêm mới thành công");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO dto) {
		try {
			categoryService.update(id, dto);
			return ResponseEntity.ok("Cập nhật thành công");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
		try {
			categoryService.delete(id);
			return ResponseEntity.ok("Đã xóa danh mục ID " + id);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Lỗi khi xóa danh mục: " + e.getMessage());
		}
	}

	@PostMapping("/preview-import")
	public ResponseEntity<?> previewImport(@RequestParam("file") MultipartFile file) {
		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();
			List<Map<String, Object>> previewList = new ArrayList<>();

			// Find column indices
			int nameCol = -1, iconCol = -1;
			Row headerRow = sheet.getRow(0);
			if (headerRow != null) {
				for (int c = 0; c < headerRow.getLastCellNum(); c++) {
					String head = formatter.formatCellValue(headerRow.getCell(c)).trim().toLowerCase();
					if (head.contains("tên") || head.contains("category") || head.contains("danh mục")) nameCol = c;
					if (head.contains("ảnh") || head.contains("icon") || head.contains("image")) iconCol = c;
				}
			}
			if (nameCol == -1) nameCol = 1;

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) continue;

				String name = formatter.formatCellValue(row.getCell(nameCol)).trim();
				String icon = (iconCol != -1) ? formatter.formatCellValue(row.getCell(iconCol)).trim() : "";

				if (name.isEmpty()) continue;

				Map<String, Object> item = new HashMap<>();
				item.put("name", name);
				item.put("icon", icon);
				item.put("exists", categoryRepository.existsByName(name));
				item.put("isValid", true);

				previewList.add(item);
			}
			return ResponseEntity.ok(previewList);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Lỗi đọc file: " + e.getMessage());
		}
	}

	@PostMapping("/confirm-import")
	public ResponseEntity<?> confirmImport(@RequestBody List<Map<String, Object>> items) {
		try {
			int count = 0;
			for (Map<String, Object> mapData : items) {
				Boolean isValid = (Boolean) mapData.get("isValid");
				Boolean exists = (Boolean) mapData.get("exists");
				if (isValid != null && isValid && (exists == null || !exists)) {
					String name = (String) mapData.get("name");
					String icon = (String) mapData.get("icon");

					CategoryEntity entity = new CategoryEntity();
					entity.setName(name);
					entity.setIcon(icon);
					categoryRepository.save(entity);
					count++;
				}
			}
			return ResponseEntity.ok("Đã nhập thành công " + count + " danh mục!");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
		}
	}

	@PostMapping("/import-excel")
	public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
		try {
			categoryService.importExcel(file);
			return ResponseEntity.ok("Nhập dữ liệu Excel thành công");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Lỗi khi nhập Excel: " + e.getMessage());
		}
	}

	@GetMapping("/export-excel")
	public ResponseEntity<byte[]> exportExcel() {
		try {
			byte[] data = categoryService.exportToExcel();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", "categories.xlsx");
			return new ResponseEntity<>(data, headers, HttpStatus.OK);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
