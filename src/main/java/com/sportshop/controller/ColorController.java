package com.sportshop.controller;

import com.sportshop.service.IColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sportshop.dto.ColorDTO;
import com.sportshop.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@CrossOrigin
@RestController
@RequestMapping("/color")
public class ColorController {
    @Autowired
    private IColorService colorService;

    @GetMapping("/all-public")
    public ResponseEntity<?> getAllPublic() {
        return ResponseEntity.ok(colorService.findAllDTO());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        // If it's a simple call without params, return all DTOs for backward compatibility
        if (search == null && page == 0 && size == 10) {
             // Check if it's actually a paginated request from admin or just a list request
             // To be safe, if 'size' is 10 and no other params, we check if it's from the old frontend
        }
        
        try {
            Sort sortObj;
            switch (sort) {
                case "oldest": sortObj = Sort.by(Sort.Direction.ASC, "createdAt"); break;
                case "az": sortObj = Sort.by(Sort.Direction.ASC, "nameColor"); break;
                case "za": sortObj = Sort.by(Sort.Direction.DESC, "nameColor"); break;
                case "newest":
                default: sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); break;
            }
            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<ColorDTO> result = colorService.findAll(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    pageable
            );
            return ResponseEntity.ok(PageResponse.of(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách: " + e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> getList() {
        return ResponseEntity.ok(colorService.findAllDTO());
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody ColorDTO dto) {
        try {
            colorService.save(dto);
            return ResponseEntity.ok("Thêm mới thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ColorDTO dto) {
        try {
            colorService.update(id, dto);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            colorService.delete(id);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] data = colorService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "colors.xlsx");
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
