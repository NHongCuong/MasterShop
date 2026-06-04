package com.sportshop.controller;

import com.sportshop.dto.CategoryDTO;
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

@CrossOrigin
@RestController
@RequestMapping("/category")
public class CategoryController {
	@Autowired
	ICategoryService categoryService;

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
