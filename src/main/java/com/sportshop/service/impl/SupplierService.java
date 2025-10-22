package com.sportshop.service.impl;


import com.sportshop.converter.SupplierConverter;
import com.sportshop.dto.SupplierDTO;
import com.sportshop.entity.SupplierEntity;
import com.sportshop.repository.SupplierRepository;
import com.sportshop.service.ICategoryService;
import com.sportshop.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierService implements ISupplierService {
	@Autowired
    SupplierRepository supplierRepo;
	@Autowired
    SupplierConverter supplierConverter;
	@Override
	public List<SupplierDTO> getAll(){
		List<SupplierEntity> list =  supplierRepo.findAll();
		List<SupplierDTO> result = new ArrayList<>();
		for(SupplierEntity en: list)
		{
            SupplierDTO dto = supplierConverter.toDTO(en);
			result.add(dto);
		}
		return result;
	}
}
