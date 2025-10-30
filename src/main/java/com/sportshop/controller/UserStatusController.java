package com.sportshop.controller;

import com.sportshop.repository.UserStatusRepository;
import com.sportshop.service.IUserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/userstatus")
public class UserStatusController {
	@Autowired
    IUserStatusService userstatusService;
    @Autowired
    private UserStatusRepository userstatusRepo;
//	@GetMapping("/all")
//	public List<CategoryDTO> all()
//	{
//		return categortService.getAll();
//	}
    @GetMapping("/all")
    public ResponseEntity<?> getAllUserStatus() {
        try {
            return new ResponseEntity<>(userstatusRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy danh sách: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//    // Lấy danh muc theo ID
//    @GetMapping("/{id}")
//    public ResponseEntity<CategoryEntity> getProductById(@PathVariable("id") Long id) {
//        CategoryEntity category = categoryRepo.findOne(id); // ✅ Spring 1.5 dùng findOne
//        if (category == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(category);
//    }
//    @PostMapping("/{add}")
//    public ResponseEntity<?> addCategory(@RequestBody CategoryEntity categoryEntity){
//        try{
//            CategoryEntity category = categoryRepo.save(categoryEntity);
//                return new ResponseEntity<>(category,HttpStatus.CREATED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Lỗi khi thêm danh mục:" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//    }
//    @PutMapping("/update/{id}")
//    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryEntity categoryEntity){
//        try{
//            CategoryEntity existing = categoryRepo.findOne(id);
//            if(existing==null){
//                return new ResponseEntity<>("Không tìm thấy ID danh mục" +id , HttpStatus.NOT_FOUND);
//            }
//
//            existing.setName(categoryEntity.getName());
//            existing.setIcon(categoryEntity.getIcon());
//
//            CategoryEntity category =categoryRepo.save(existing);
//            return new ResponseEntity<>(category, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Lỗi khi cập nhật danh mục: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    @DeleteMapping("/delete/{id}")
//    public ResponseEntity<?> deleteCategory(@PathVariable Long id){
//        try{
//            categoryRepo.delete(id);
//            return new ResponseEntity<>("Đã xóa danh mục ID" +id, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Lỗi khi xóa danh mục" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
}
