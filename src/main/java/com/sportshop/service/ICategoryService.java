package com.sportshop.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sportshop.dto.CategoryDTO;

public interface ICategoryService {
	List<CategoryDTO> getAll();
	Page<CategoryDTO> findAll(String search, Pageable pageable);
	CategoryDTO get(Long id);
	void save(CategoryDTO dto);
	void update(Long id, CategoryDTO dto);
	void delete(Long id);
	byte[] exportToExcel() throws Exception;
	void importExcel(org.springframework.web.multipart.MultipartFile file) throws Exception;
}
