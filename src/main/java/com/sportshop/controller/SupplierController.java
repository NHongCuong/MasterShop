package com.sportshop.controller;

import com.sportshop.dto.SupplierDTO;
import com.sportshop.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/supplier")
public class SupplierController {
	@Autowired
    ISupplierService supplierService;
	@GetMapping("/all")
	public List<SupplierDTO> all()
	{
		return supplierService.getAll();
	}
}
