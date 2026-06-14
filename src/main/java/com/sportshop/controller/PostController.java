package com.sportshop.controller;

import com.sportshop.entity.CategoryEntity;
import com.sportshop.entity.PostEntity;
import com.sportshop.repository.CategoryRepository;
import com.sportshop.repository.PostRepository;
import com.sportshop.response.PageResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/post")
@CrossOrigin
public class PostController {

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @GetMapping("/admin/all")
    public ResponseEntity<PageResponse<PostEntity>> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "newest") String sort) {

        Sort sortObj = Sort.by("id").descending();
        if ("oldest".equals(sort)) {
            sortObj = Sort.by("id").ascending();
        }

        Page<PostEntity> result;
        if (categoryName != null && !categoryName.isEmpty() && !categoryName.equals("Tất cả danh mục")) {
            result = postRepo.findByCategoryAndSearch(categoryName, search, PageRequest.of(page, size, sortObj));
        } else {
            result = postRepo.findWithSearch(search, PageRequest.of(page, size, sortObj));
        }

        return ResponseEntity.ok(PageResponse.of(result));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<PostEntity>> getAllPublic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryName) {
        
        Page<PostEntity> result;
        if (categoryName != null && !categoryName.isEmpty()) {
            result = postRepo.findByCategoryAndSearch(categoryName, search, PageRequest.of(page, size, Sort.by("id").descending()));
        } else {
            result = postRepo.findWithSearch(search, PageRequest.of(page, size, Sort.by("id").descending()));
        }
        return ResponseEntity.ok(PageResponse.of(result));
    }

    // Helper to generate slug from title
    private String slugify(String text) {
        if (text == null || text.isEmpty()) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase()
            .replaceAll("đ", "d")
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        return normalized;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return postRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return postRepo.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/create")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> createFromMap(@RequestBody Map<String, Object> payload) {
        try {
            PostEntity post = new PostEntity();
            post.setTitle((String) payload.get("title"));
            String slug = (String) payload.get("slug");
            if (slug == null || slug.isBlank()) {
                slug = slugify((String) payload.get("title")) + "-" + System.currentTimeMillis();
            }
            post.setSlug(slug);
            post.setImage((String) payload.get("image"));
            post.setShortDescription((String) payload.get("shortDescription"));
            post.setContent((String) payload.get("content"));
            post.setAuthorName((String) payload.get("authorName"));

            Map<String, Object> catMap = (Map<String, Object>) payload.get("category");
            if (catMap != null && catMap.get("id") != null) {
                Long catId = Long.valueOf(catMap.get("id").toString());
                categoryRepo.findById(catId).ifPresent(post::setCategory);
            }

            postRepo.save(post);
            return ResponseEntity.ok("Thêm thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage() + (e.getCause() != null ? " | Cause: " + e.getCause().getMessage() : ""));
        }
    }

    @PutMapping("/admin/update/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> updateFromMap(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            PostEntity post = postRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
            
            if (payload.containsKey("title")) post.setTitle((String) payload.get("title"));
            if (payload.containsKey("slug")) {
                String slug = (String) payload.get("slug");
                if (slug == null || slug.isBlank()) {
                    slug = slugify(post.getTitle()) + "-" + post.getId();
                }
                post.setSlug(slug);
            }
            if (payload.containsKey("image")) post.setImage((String) payload.get("image"));
            if (payload.containsKey("shortDescription")) post.setShortDescription((String) payload.get("shortDescription"));
            if (payload.containsKey("content")) post.setContent((String) payload.get("content"));
            if (payload.containsKey("authorName")) post.setAuthorName((String) payload.get("authorName"));

            if (payload.containsKey("category")) {
                Map<String, Object> catMap = (Map<String, Object>) payload.get("category");
                if (catMap != null && catMap.get("id") != null) {
                    Long catId = Long.valueOf(catMap.get("id").toString());
                    categoryRepo.findById(catId).ifPresent(post::setCategory);
                } else {
                    post.setCategory(null);
                }
            }

            post.setUpdatedAt(new Date());
            postRepo.save(post);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage() + (e.getCause() != null ? " | Cause: " + e.getCause().getMessage() : ""));
        }
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        postRepo.deleteById(id);
        return ResponseEntity.ok("Đã xoá");
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=posts.xlsx";
        response.setHeader(headerKey, headerValue);

        List<PostEntity> list = postRepo.findAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Posts");
            Row headerRow = sheet.createRow(0);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"STT", "Ảnh", "Tiêu đề", "Nội dung tóm tắt", "Danh mục", "Tác giả", "Ngày tạo", "Ngày sửa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowCount = 1;
            for (PostEntity item : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(rowCount);
                row.createCell(1).setCellValue(item.getImage() != null ? item.getImage() : "");
                row.createCell(2).setCellValue(item.getTitle() != null ? item.getTitle() : "");
                row.createCell(3).setCellValue(item.getShortDescription() != null ? item.getShortDescription() : "");
                row.createCell(4).setCellValue(item.getCategory() != null ? item.getCategory().getName() : "");
                row.createCell(5).setCellValue(item.getAuthorName() != null ? item.getAuthorName() : "");
                row.createCell(6).setCellValue(item.getCreatedAt() != null ? sdf.format(item.getCreatedAt()) : "");
                row.createCell(7).setCellValue(item.getUpdatedAt() != null ? sdf.format(item.getUpdatedAt()) : "");
                rowCount++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @PostMapping("/admin/import-excel")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String title = formatter.formatCellValue(row.getCell(2)); // Col 2 is title
                if (title.isEmpty()) continue;

                PostEntity post = new PostEntity();
                post.setImage(formatter.formatCellValue(row.getCell(1)));
                post.setTitle(title);
                post.setShortDescription(formatter.formatCellValue(row.getCell(3)));
                
                String catName = formatter.formatCellValue(row.getCell(4));
                if (!catName.isEmpty()) {
                    Optional<CategoryEntity> cat = categoryRepo.findFirstByName(catName);
                    cat.ifPresent(post::setCategory);
                }
                
                post.setAuthorName(formatter.formatCellValue(row.getCell(5)));
                postRepo.save(post);
            }
            workbook.close();
            return ResponseEntity.ok("Nhập thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi nhập dữ liệu: " + e.getMessage());
        }
    }
}
