package com.sportshop.controller;

import java.util.Optional;
import com.sportshop.converter.UserConverter;
import com.sportshop.entity.*;
import com.sportshop.repository.UserRepository;
import com.sportshop.repository.UserStatusRepository;
import com.sportshop.repository.UserTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.sportshop.config.JwtTokenUtils;
import com.sportshop.dto.UserDTO;
import com.sportshop.response.AuthResponse;
import com.sportshop.security.MyUserDetails;
import com.sportshop.service.IUserService;
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
//	public List<UserDTO> getAll()
//	{
//		return userService.getAll();
//	}

    // Lấy users theo ID
    @GetMapping("/users/{id}")
    public ResponseEntity<UserEntity> getProductById(@PathVariable("id") Long id) {
        UserEntity user = userRepository.findOne(id); // ✅ Spring 1.5 dùng findOne
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
            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
            String hash = bcrypt.encode(userDTO.getPassword());

            // ✅ Chuyển DTO sang Entity
            UserEntity entity = userConverter.toEntity(userDTO);

            // ✅ Gán mật khẩu đã mã hóa
            entity.setPassword(hash);

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
            UserEntity existing = userRepository.findOne(id);
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
            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
            String hash = bcrypt.encode(user.getPassword());

            // ✅ Chuyển DTO sang Entity
            //UserEntity entity = userConverter.toEntity(userDTO);

            // ✅ Gán mật khẩu đã mã hóa
            existing.setPassword(hash);

            existing.setNameUser(user.getNameUser());
            existing.setPhone(user.getPhone());
            //existing.setPassword(user.getPassword());
            existing.setEmail(user.getEmail());
            existing.setAddress(user.getAddress());
            existing.setRegtime(user.getRegtime());
            existing.setSalt(user.getSalt());
            existing.setVerify(user.getVerify());

            if (user.getUserType() != null && user.getUserType().getId() != null) {
                UserTypeEntity usertype = usertypeRepo.findOne(user.getUserType().getId());
                existing.setUserType(usertype);
            }

            if (user.getUserStatus() != null && user.getUserStatus().getId() != null) {
                UserStatusEntity userstatus = userstatusRepo.findOne(user.getUserStatus().getId());
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
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userRepository.delete(id);
            return new ResponseEntity<>("Đã xóa người dùng ID " + id, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi xóa: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody UserDTO userDTO) throws Exception
	{
		_authenticate(userDTO.getEmail(), userDTO.getPassword());
		final MyUserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getEmail());
		String token = jwtTokenUtils.generateToken(userDetails);
		UserDTO userInfo = userService.loadUserByEmail(userDTO.getEmail());
		return ResponseEntity.ok(new AuthResponse(200, "Login is successful",userInfo, token));
	}
	@PostMapping("/authenticate")
	public ResponseEntity<AuthResponse> authenticate(Authentication authentication) throws Exception
	{
        try{
            String email = authentication.getName(); // 'example@yahoo.com'
            UserDTO userInfo = userService.loadUserByEmail(email);
            return ResponseEntity.ok(new AuthResponse(200, "Authenticated",userInfo));
        } catch (Exception e) {
            throw new Exception(e);
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