package com.sportshop.controller;

import java.util.Map;
import java.util.Optional;
import com.sportshop.converter.UserConverter;
import com.sportshop.entity.*;
import com.sportshop.repository.UserRepository;
import com.sportshop.repository.UserStatusRepository;
import com.sportshop.repository.UserTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.sportshop.config.JwtTokenUtils;
import com.sportshop.dto.UserDTO;
import com.sportshop.response.AuthResponse;
import com.sportshop.response.PageResponse;
import com.sportshop.security.MyUserDetails;
import com.sportshop.service.IUserService;
import com.sportshop.service.UserPasswordService;
import com.sportshop.service.impl.MyUserDetailsService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class UserController {
	@Autowired
	IUserService userService;
	@Autowired
	MyUserDetailsService userDetailsService;
	@Autowired
	AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTypeRepository usertypeRepo;
    @Autowired
    private UserStatusRepository userstatusRepo;
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private UserPasswordService userPasswordService;
	@Autowired
	JwtTokenUtils jwtTokenUtils;

	@GetMapping("/user")
    public ResponseEntity<?> getAllProducts() {
        try {
            return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/all")
    public ResponseEntity<?> getAllPaginated(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        try {
            Sort sortObj;
            switch (sort) {
                case "oldest":
                    sortObj = Sort.by(Sort.Direction.ASC, "id");
                    break;
                case "az":
                    sortObj = Sort.by(Sort.Direction.ASC, "nameUser");
                    break;
                case "za":
                    sortObj = Sort.by(Sort.Direction.DESC, "nameUser");
                    break;
                case "newest":
                default:
                    sortObj = Sort.by(Sort.Direction.DESC, "id");
                    break;
            }
            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<UserDTO> result = userService.findAll(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    pageable);
            return ResponseEntity.ok(PageResponse.of(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách: " + e.getMessage());
        }
    }

    @GetMapping("/user/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] data = userService.exportToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "users.xlsx");
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserEntity> getProductById(@PathVariable("id") Long id) {
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody UserDTO userDTO) {
        try {
            Optional<UserEntity> userData = userRepository.findByEmail(userDTO.getEmail());
            if(userData.isPresent())
            {
                return new ResponseEntity<>("Email đã tồn tại!" , HttpStatus.BAD_REQUEST);
            }
            String salt = userPasswordService.generateSalt();
            String hash = userPasswordService.encode(userDTO.getPassword(), salt);
            UserEntity entity = userConverter.toEntity(userDTO);
            entity.setPassword(hash);
            entity.setSalt(salt);
            UserEntity savedUser = userRepository.save(entity);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserEntity user) {
        try {
            UserEntity existing = userRepository.findById(id).orElse(null);
            if (existing == null) {
                return new ResponseEntity<>("Không tìm thấy User ID " + id, HttpStatus.NOT_FOUND);
            }
            if (!existing.getEmail().equals(user.getEmail())) {
                Optional<UserEntity> userData = userRepository.findByEmail(user.getEmail());
                if (userData.isPresent()) {
                    return new ResponseEntity<>("Email đã tồn tại!", HttpStatus.BAD_REQUEST);
                }
            }
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                String salt = userPasswordService.generateSalt();
                String hash = userPasswordService.encode(user.getPassword(), salt);
                existing.setPassword(hash);
                existing.setSalt(salt);
            }
            existing.setNameUser(user.getNameUser());
            existing.setPhone(user.getPhone());
            existing.setEmail(user.getEmail());
            existing.setAddress(user.getAddress());
            existing.setVerify(user.getVerify());
            existing.setAvatar(user.getAvatar());

            if (user.getUserType() != null && user.getUserType().getId() != null) {
                UserTypeEntity usertype = usertypeRepo.findById(user.getUserType().getId()).orElse(null);
                existing.setUserType(usertype);
            }
            if (user.getUserStatus() != null && user.getUserStatus().getId() != null) {
                UserStatusEntity userstatus = userstatusRepo.findById(user.getUserStatus().getId()).orElse(null);
                existing.setUserStatus(userstatus);
            }
            UserEntity updated = userRepository.save(existing);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi cập nhật: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> passwords) {
        try {
            String newPassword = passwords.get("newPassword");
            UserEntity user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return new ResponseEntity<>("Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
            }
            String newSalt = userPasswordService.generateSalt();
            String newHash = userPasswordService.encode(newPassword, newSalt);
            user.setPassword(newHash);
            user.setSalt(newSalt);
            userRepository.save(user);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>("Đã xóa người dùng ID " + id, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi xóa: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody UserDTO userDTO) throws Exception {
        try {
            AuthResponse authResult = userService.login(userDTO);
            if (authResult.status != 200) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResult);
            }
            final MyUserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getEmail());
            String token = jwtTokenUtils.generateToken(userDetails);
            UserDTO userInfo = userService.loadUserByEmail(userDTO.getEmail());
            return ResponseEntity.ok(new AuthResponse(200, "Login is successful", userInfo, token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(401, "Login failed: " + e.getMessage()));
        }
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthResponse> authenticate(Authentication authentication) throws Exception {
        try{
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(401, "Not authenticated"));
            }
            String email = authentication.getName();
            UserDTO userInfo = userService.loadUserByEmail(email);
            return ResponseEntity.ok(new AuthResponse(200, "Authenticated",userInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse(500, "Error: " + e.getMessage()));
        }
	}

	@PutMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@RequestBody UserDTO userDTO) {
		AuthResponse r = userService.signup(userDTO);
		if(r.status == 200) return ResponseEntity.ok(r);
		else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
	}

    @PostMapping("/user/preview-import")
    public ResponseEntity<?> previewImport(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<Map<String, Object>> previewList = new ArrayList<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> item = new java.util.HashMap<>();
                item.put("nameUser", formatter.formatCellValue(row.getCell(0)));
                item.put("phone", formatter.formatCellValue(row.getCell(1)));
                String email = formatter.formatCellValue(row.getCell(2));
                if (email == null || email.isBlank()) continue;
                item.put("email", email);
                item.put("address", formatter.formatCellValue(row.getCell(3)));
                
                String typeName = formatter.formatCellValue(row.getCell(4));
                Map<String, Object> typeMap = new java.util.HashMap<>();
                typeMap.put("name", typeName);
                if (!typeName.isEmpty()) {
                    Optional<UserTypeEntity> ut = usertypeRepo.findByName(typeName);
                    if (ut.isPresent()) typeMap.put("id", ut.get().getId());
                }
                item.put("userType", typeMap);
                
                String statusName = formatter.formatCellValue(row.getCell(5));
                Map<String, Object> statusMap = new java.util.HashMap<>();
                statusMap.put("name", statusName);
                if (!statusName.isEmpty()) {
                    Optional<UserStatusEntity> us = userstatusRepo.findByName(statusName);
                    if (us.isPresent()) statusMap.put("id", us.get().getId());
                }
                item.put("userStatus", statusMap);
                
                item.put("password", formatter.formatCellValue(row.getCell(6)));
                previewList.add(item);
            }
            workbook.close();
            return ResponseEntity.ok(previewList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi preview: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/user/confirm-import")
    public ResponseEntity<?> confirmImport(@RequestBody List<Map<String, Object>> users) {
        try {
            int count = 0;
            for (Map<String, Object> userData : users) {
                String email = (String) userData.get("email");
                if (userRepository.findByEmail(email).isPresent()) continue;

                UserEntity user = new UserEntity();
                user.setNameUser((String) userData.get("nameUser"));
                user.setPhone((String) userData.get("phone"));
                user.setEmail(email);
                user.setAddress((String) userData.get("address"));
                
                Map<String, Object> typeMap = (Map<String, Object>) userData.get("userType");
                if (typeMap != null && typeMap.get("name") != null) {
                    user.setUserType(usertypeRepo.findByName((String) typeMap.get("name")).orElse(null));
                }
                Map<String, Object> statusMap = (Map<String, Object>) userData.get("userStatus");
                if (statusMap != null && statusMap.get("name") != null) {
                    user.setUserStatus(userstatusRepo.findByName((String) statusMap.get("name")).orElse(null));
                }
                
                String rawPwd = (String) userData.get("password");
                if (rawPwd == null || rawPwd.isBlank()) rawPwd = "123";
                String salt = userPasswordService.generateSalt();
                String hash = userPasswordService.encode(rawPwd, salt);
                user.setPassword(hash);
                user.setSalt(salt);
                
                userRepository.save(user);
                count++;
            }
            return ResponseEntity.ok("Nhập thành công " + count + " người dùng!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }
}
