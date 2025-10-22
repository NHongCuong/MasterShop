package com.sportshop.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sportshop.converter.ProductConverter;
import com.sportshop.dto.ProductDTO;
import com.sportshop.entity.ProductEntity;
import com.sportshop.repository.ProductRepository;
import com.sportshop.service.IProductService;

@Service
public class ProductService implements IProductService{
    @Autowired
    ProductRepository productRepo;
    @Autowired
    ProductConverter productConverter;
    @Override
    public List<ProductDTO> getAll(){
        List<ProductEntity> list =  productRepo.findAll();
        List<ProductDTO> result = new ArrayList<>();
        for(ProductEntity en: list)
        {
            ProductDTO dto = productConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public ProductDTO get(Long id) {
        ProductEntity product = productRepo.findOne(id);
        ProductDTO dto = productConverter.toDTO(product);
        return dto;
    }
//    @Override
//    public ProductDTO get(Long id) {
//        // findById trả về Optional<ProductEntity>
//        ProductEntity product = productRepo.findById(id).orElse(null);
//        if (product == null) {
//            return null; // hoặc throw exception tùy logic bạn muốn
//        }
//        return productConverter.toDTO(product);
//    }
}
