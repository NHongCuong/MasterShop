package com.sportshop.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.sportshop.converter.UserConverter;
import com.sportshop.dto.UserDTO;
import com.sportshop.entity.UserEntity;
import com.sportshop.entity.UserStatusEntity;
import com.sportshop.entity.UserTypeEntity;
import com.sportshop.repository.UserRepository;
import com.sportshop.response.AuthResponse;
import com.sportshop.service.IUserService;
import com.sportshop.service.UserPasswordService;

@Service
public class UserService implements IUserService{

	@Autowired
	UserRepository userRepo;
	@Autowired
	UserConverter userConverter;
	@Autowired
	UserPasswordService userPasswordService;
	@Override
	public List<UserDTO> getAll() {
		List<UserEntity> list = userRepo.findAll();
		List<UserDTO> listDTO = new ArrayList<UserDTO>();
		for(UserEntity en: list)
		{
			UserDTO dto = userConverter.toDTO(en);
			listDTO.add(dto);
		}
		return listDTO;
	}

	@Override
	public Page<UserDTO> findAll(String search, Pageable pageable) {
		Page<UserEntity> page = userRepo.findWithSearch(search, pageable);
		return page.map(userConverter::toDTO);
	}

	@Override
	public byte[] exportToExcel() throws Exception {
		List<UserDTO> allData = getAll();

		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Users");

			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerStyle.setFont(headerFont);
			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);

			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);

			Row headerRow = sheet.createRow(0);
			String[] headers = {"Tên", "SĐT", "Email", "Địa chỉ", "Loại tài khoản", "Trạng thái", "Mật khẩu", "Ngày tạo", "Ngày sửa"};
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
			}

			int rowNum = 1;
			for (UserDTO dto : allData) {
				Row row = sheet.createRow(rowNum);
				Cell c0 = row.createCell(0); c0.setCellValue(dto.getNameUser() != null ? dto.getNameUser() : ""); c0.setCellStyle(dataStyle);
				Cell c1 = row.createCell(1); c1.setCellValue(dto.getPhone() != null ? dto.getPhone() : ""); c1.setCellStyle(dataStyle);
				Cell c2 = row.createCell(2); c2.setCellValue(dto.getEmail() != null ? dto.getEmail() : ""); c2.setCellStyle(dataStyle);
				Cell c3 = row.createCell(3); c3.setCellValue(dto.getAddress() != null ? dto.getAddress() : ""); c3.setCellStyle(dataStyle);
				Cell c4 = row.createCell(4);
				c4.setCellValue(dto.getUserType() != null && dto.getUserType().getName() != null ? dto.getUserType().getName() : "");
				c4.setCellStyle(dataStyle);
				Cell c5 = row.createCell(5);
				c5.setCellValue(dto.getUserStatus() != null && dto.getUserStatus().getName() != null ? dto.getUserStatus().getName() : "");
				c5.setCellStyle(dataStyle);
				Cell c6 = row.createCell(6); c6.setCellValue(""); c6.setCellStyle(dataStyle); // Password
				Cell c7 = row.createCell(7); c7.setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : ""); c7.setCellStyle(dataStyle);
				Cell c8 = row.createCell(8); c8.setCellValue(dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : ""); c8.setCellStyle(dataStyle);
				rowNum++;
			}

			for (int i = 0; i < headers.length; i++) {
				sheet.autoSizeColumn(i);
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();
		}
	}

	@Override
	public UserDTO loadUserByEmail(String email) {
		Optional<UserEntity> en = userRepo.findByEmail(email);
		if(en.isPresent())
			return userConverter.toDTO(en.get());
		return null;
	}
	@Override
	public AuthResponse login(UserDTO userDTO) {
		//Check email
		Optional<UserEntity> userData = userRepo.findByEmail(userDTO.getEmail());
		if(userData.isPresent())
		{
			UserEntity entity = userData.get();
			if(userPasswordService.matches(userDTO.getPassword(), entity.getSalt(), entity.getPassword()))
			{
				return new AuthResponse(200, "Login is successful");
			}
			return new AuthResponse(401, "Password is wrong");
		}
		return new AuthResponse(401, "Password is wrong");
	}
	@Override
	public AuthResponse signup(UserDTO userDTO) {
		//Check email is existed
		Optional<UserEntity> userData = userRepo.findByEmail(userDTO.getEmail());
		if(userData.isPresent())
		{
			return new AuthResponse(400, "This email is existed");
		}
		else {
			String salt = userPasswordService.generateSalt();
			String hash = userPasswordService.encode(userDTO.getPassword(), salt);
			UserEntity entity = userConverter.toEntity(userDTO);
			entity.setPassword(hash);
			entity.setSalt(salt);
			entity.setUserStatus(new UserStatusEntity(1L));
			entity.setUserType(new UserTypeEntity(1L));
			userRepo.save(entity);
			return new AuthResponse(200, "Signup is successful");
		}
	}
}
