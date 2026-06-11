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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import com.sportshop.config.JwtTokenUtils;
import com.sportshop.dto.UserDTO;
import com.sportshop.response.AuthResponse;
import com.sportshop.response.PageResponse;
import com.sportshop.security.MyUserDetails;
import com.sportshop.service.IUserService;
import com.sportshop.service.UserPasswordService;
import com.sportshop.service.impl.MyUserDetailsService;

//@RequestMapping("/user")
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
   // @Autowired
   //private PasswordEncoder passwordEncoder;
	@Autowired
	JwtTokenUtils jwtTokenUtils;
	@GetMapping("/user")
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
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
//	public List<UserDTO> getAll()
//	{
//		return userService.getAll();
//	}

    // Lấy users theo ID
    @GetMapping("/users/{id}")
    public ResponseEntity<UserEntity> getProductById(@PathVariable("id") Long id) {
        UserEntity user = userRepository.findById(id).orElse(null); // ✅ Spring 1.5 dùng findOne
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

//    @PostMapping("/add")
//    public ResponseEntity<?> addUser(@RequestBody UserEntity userEntity){
//        try{
//            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
//            String hash = bcrypt.encode(userEntity.getPassword());
//            UserEntity entity = userConverter.toDTO(userEntity);
//            entity.setPassword(hash);
//
//
//            UserEntity user = userRepository.save(userEntity);
//
//            return new ResponseEntity<>(user,HttpStatus.CREATED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Lỗi khi thêm user:" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody UserDTO userDTO) {
        try {

            Optional<UserEntity> userData = userRepository.findByEmail(userDTO.getEmail());
            if(userData.isPresent())
            {
                return new ResponseEntity<>("Email đã tồn tại!" , HttpStatus.BAD_GATEWAY);
            }
            // ✅ Mã hóa mật khẩu bằng BCryptPasswordEncoder (tạo trực tiếp)
            String salt = userPasswordService.generateSalt();
            String hash = userPasswordService.encode(userDTO.getPassword(), salt);

            // ✅ Chuyển DTO sang Entity
            UserEntity entity = userConverter.toEntity(userDTO);

            // ✅ Gán mật khẩu đã mã hóa
            entity.setPassword(hash);
            entity.setSalt(salt);

            // ✅ Lưu vào DB
            UserEntity savedUser = userRepository.save(entity);

            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi khi thêm user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // 🟡 API sửa sản phẩm
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserEntity user) {
        try {
            UserEntity existing = userRepository.findById(id).orElse(null);
            if (existing == null) {
                return new ResponseEntity<>("Không tìm thấy User ID " + id, HttpStatus.NOT_FOUND);
            }
            // 🔹 Kiểm tra nếu email mới khác email cũ
            if (!existing.getEmail().equals(user.getEmail())) {
                Optional<UserEntity> userData = userRepository.findByEmail(user.getEmail());
                if (userData.isPresent()) {
                    return new ResponseEntity<>("Email đã tồn tại!", HttpStatus.BAD_GATEWAY);
                }
            }
            // ✅ Mã hóa mật khẩu bằng BCryptPasswordEncoder (tạo trực tiếp)
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                String salt = userPasswordService.generateSalt();
                String hash = userPasswordService.encode(user.getPassword(), salt);
                existing.setPassword(hash);
                existing.setSalt(salt);
            }

            // ✅ Chuyển DTO sang Entity
            //UserEntity entity = userConverter.toEntity(userDTO);

            // ✅ Gán mật khẩu đã mã hóa
            existing.setNameUser(user.getNameUser());
            existing.setPhone(user.getPhone());
            //existing.setPassword(user.getPassword());
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
    // 🔴 API xóa user
    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> passwords) {
        try {
            String newPassword = passwords.get("newPassword");
            
            UserEntity user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return new ResponseEntity<>("Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
            }
            
            // Generate new salt and encode
            String newSalt = userPasswordService.generateSalt();
            String newHash = userPasswordService.encode(newPassword, newSalt);
            
            user.setPassword(newHash);
            user.setSalt(newSalt);
            
            userRepository.save(user);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
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
	public ResponseEntity<AuthResponse> login(@RequestBody UserDTO userDTO) throws Exception
	{
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(401, "Login failed: " + e.getMessage()));
        }
	}
	@PostMapping("/authenticate")
	public ResponseEntity<AuthResponse> authenticate(Authentication authentication) throws Exception
	{
        try{
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(401, "Not authenticated"));
            }
            String email = authentication.getName(); // 'example@yahoo.com'
            UserDTO userInfo = userService.loadUserByEmail(email);
            return ResponseEntity.ok(new AuthResponse(200, "Authenticated",userInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(500, "Error: " + e.getMessage()));
        }

	}
	@PutMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@RequestBody UserDTO userDTO) {
		AuthResponse r = userService.signup(userDTO);
		if(r.status == 200)
			return ResponseEntity.ok(r);
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(r);
	}
	private void _authenticate(String email, String password) throws Exception {
        try {
            // Load user details by email
            MyUserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Authenticate using email and password
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password, userDetails.getAuthorities()));
        } catch (AuthenticationException e) {
            // Authentication failed
            throw new Exception("Invalid credentials", e);
        }
    }
	
}
