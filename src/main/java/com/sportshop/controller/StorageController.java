package com.sportshop.controller;

import com.sportshop.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private CloudinaryService cloudinaryService;

    private String imageFolder = "D:\\Programing\\images";

    // ✅ API Upload ảnh lên Cloudinary
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, 
                                        @RequestParam(value = "folder", defaultValue = "products") String folder) {
        try {
            String url = cloudinaryService.uploadFile(file, folder);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi upload: " + e.getMessage());
        }
    }

    // 🟡 Giữ lại các API lấy ảnh cũ để tương thích ngược với dữ liệu cũ trong DB
    @GetMapping("/images/products/{folder}/{fileName:.+}")
    public byte[] getImageProduct(@PathVariable String folder, @PathVariable String fileName) throws IOException {
        String pathImage = imageFolder + "\\products\\" + folder + "\\" + fileName;
        File file = new File(pathImage);
        if (file.exists()) {
            return Files.readAllBytes(file.toPath());
        }
        return null;
    }

    @GetMapping("/images/categories/{fileName:.+}")
    public byte[] getImageCategory(@PathVariable String fileName) throws IOException {
        String pathImage = imageFolder + "\\categories\\" + fileName;
        File file = new File(pathImage);
        if (file.exists()) {
            return Files.readAllBytes(file.toPath());
        }
        return null;
    }
}
