package com.sportshop.service.impl;

import com.sportshop.dto.GeneralImageDTO;
import com.sportshop.entity.GeneralImageEntity;
import com.sportshop.repository.GeneralImageRepository;
import com.sportshop.service.IGeneralImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GeneralImageService implements IGeneralImageService {

    @Autowired
    private GeneralImageRepository generalImageRepository;

    @Override
    public List<GeneralImageDTO> getAllImages() {
        return generalImageRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public GeneralImageDTO getImageByName(String name) {
        Optional<GeneralImageEntity> entity = generalImageRepository.findByImageName(name);
        return entity.map(this::mapToDTO).orElse(null);
    }

    @Override
    public GeneralImageDTO saveImage(GeneralImageDTO dto) {
        GeneralImageEntity entity = new GeneralImageEntity();
        if (dto.getId() != null) {
            entity = generalImageRepository.findById(dto.getId()).orElse(new GeneralImageEntity());
        } else {
            entity = generalImageRepository.findByImageName(dto.getImageName()).orElse(new GeneralImageEntity());
        }
        
        entity.setImageName(dto.getImageName());
        entity.setImageUrl(dto.getImageUrl());
        
        GeneralImageEntity saved = generalImageRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public void deleteImage(Long id) {
        generalImageRepository.deleteById(id);
    }

    private GeneralImageDTO mapToDTO(GeneralImageEntity entity) {
        return GeneralImageDTO.builder()
                .id(entity.getId())
                .imageName(entity.getImageName())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
