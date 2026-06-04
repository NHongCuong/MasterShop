package com.sportshop.controller;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.response.PageResponse;
import com.sportshop.service.IDetailProductColorService;
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
@RequestMapping("/admin/product-color")
public class DetailProductColorController {

    @Autowired
    private IDetailProductColorService detailProductColorService;

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
                    sortObj = Sort.by(Sort.Direction.ASC, "detailColor.nameColor");
                    break;
                case "za":
                    sortObj = Sort.by(Sort.Direction.DESC, "detailColor.nameColor");
                    break;
                case "newest":
                default:
                    sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }

            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<DetailProductColorDTO> result = detailProductColorService.findAll(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    pageable
            );
            return ResponseEntity.ok(PageResponse.of(result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long idColor, @RequestParam Long idProduct) {
        try {
            detailProductColorService.delete(idColor, idProduct);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody DetailProductColorDTO dto) {
        try {
            detailProductColorService.save(dto);
            return ResponseEntity.ok("Thêm mới thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestParam Long oldIdColor, @RequestParam Long oldIdProduct,
                                   @RequestBody DetailProductColorDTO dto) {
        try {
            // Với composite key, "update" thường là xóa cũ thêm mới nếu ID thay đổi
            detailProductColorService.update(oldIdColor, oldIdProduct, dto);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] excelData = detailProductColorService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "product_colors.xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
