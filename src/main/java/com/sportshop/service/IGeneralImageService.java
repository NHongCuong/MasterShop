package com.sportshop.service;

import com.sportshop.dto.GeneralImageDTO;
import java.util.List;

public interface IGeneralImageService {
    List<GeneralImageDTO> getAllImages();
    GeneralImageDTO getImageByName(String name);
    GeneralImageDTO saveImage(GeneralImageDTO imageDTO);
    void deleteImage(Long id);
}
