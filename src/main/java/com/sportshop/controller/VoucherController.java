package com.sportshop.controller;

import com.sportshop.dto.VoucherDTO;
import com.sportshop.response.PageResponse;
import com.sportshop.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    IVoucherService voucherService;

    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        try {
            Sort sortObj;
            switch (sort) {
                case "oldest": sortObj = Sort.by(Sort.Direction.ASC, "createdAt"); break;
                case "az":     sortObj = Sort.by(Sort.Direction.ASC, "maVoucher"); break;
                case "za":     sortObj = Sort.by(Sort.Direction.DESC, "maVoucher"); break;
                default:       sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
            }
            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<VoucherDTO> result = voucherService.findAll(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    pageable);
            return ResponseEntity.ok(PageResponse.of(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getList() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    @GetMapping("/check/{code}")
    public ResponseEntity<?> checkVoucher(@PathVariable String code) {
        VoucherDTO dto = voucherService.getByCode(code);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mã voucher không hợp lệ");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        VoucherDTO dto = voucherService.get(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody VoucherDTO dto) {
        try {
            voucherService.save(dto);
            return ResponseEntity.ok("Thêm voucher thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody VoucherDTO dto) {
        try {
            voucherService.update(id, dto);
            return ResponseEntity.ok("Cập nhật voucher thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            voucherService.delete(id);
            return ResponseEntity.ok("Đã xóa voucher ID " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
