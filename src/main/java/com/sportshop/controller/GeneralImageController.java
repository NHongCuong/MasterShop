package com.sportshop.controller;

import com.sportshop.dto.GeneralImageDTO;
import com.sportshop.service.IGeneralImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/general-images")
@CrossOrigin("*")
public class GeneralImageController {

    @Autowired
    private IGeneralImageService generalImageService;

    @GetMapping
    public ResponseEntity<List<GeneralImageDTO>> getAllImages() {
        return ResponseEntity.ok(generalImageService.getAllImages());
    }

    @GetMapping("/{name}")
    public ResponseEntity<GeneralImageDTO> getImageByName(@PathVariable String name) {
        GeneralImageDTO dto = generalImageService.getImageByName(name);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<GeneralImageDTO> saveImage(@RequestBody GeneralImageDTO imageDTO) {
        return ResponseEntity.ok(generalImageService.saveImage(imageDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        generalImageService.deleteImage(id);
        return ResponseEntity.ok().build();
    }
}
