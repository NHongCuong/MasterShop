package com.sportshop.service;

import com.sportshop.dto.DimensionsDTO;
import com.sportshop.entity.DimensionsEntity;
import com.sportshop.repository.DimensionsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DimensionsService implements IDimensionsService {

    private final DimensionsRepository repository;

    public DimensionsService(DimensionsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DimensionsDTO> getAllDimensionsByProduct(Long idProduct) {
        List<DimensionsEntity> entities = repository.findByDemensions_Id(idProduct);
        return convertToDTOList(entities);
    }

    @Override
    public List<DimensionsDTO> getAllDimensions() {
        List<DimensionsEntity> entities = repository.findAll();
        return convertToDTOList(entities);
    }

    private List<DimensionsDTO> convertToDTOList(List<DimensionsEntity> entities) {
        List<DimensionsDTO> dtos = new ArrayList<>();
        for (DimensionsEntity entity : entities) {
            DimensionsDTO dto = new DimensionsDTO();
            dto.setId(entity.getId());
            dto.setNameD(entity.getNameD());
            dto.setCreatedAt(entity.getCreated_at());
            dto.setUpdatedAt(entity.getUpdated_at());
            if (entity.getDemensions() != null) {
                dto.setIdProduct(entity.getDemensions().getId());
            }
            dtos.add(dto);
        }
        return dtos;
    }
}
