package com.sportshop.controller;

import com.sportshop.entity.*;
import com.sportshop.repository.*;
import com.sportshop.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OderRepository orderRepo;

    @Autowired
    private BillRepository billRepo;

    @Autowired
    private MethodOfPaymentRepository mopRepo;

    @Autowired
    private ShipMethodRepository smRepo;

    @Autowired
    private CartDetailRepository cartDetailRepo;

    @Autowired
    private BillStatusRepository billStatusRepo;

    @Autowired
    private OderDetailRepository orderDetailRepo;

    @Autowired
    private CartStatusRepository cartStatusRepo;

    @Autowired
    private ShopcartRepository shopcartRepo;

    @Autowired
    private ColorRepository colorRepo;

    @Autowired
    private MaterialRepository materialRepo;

    @Autowired
    private DimensionsRepository dimensionsRepo;

    @Autowired
    private ProductRepository productRepo;

    @GetMapping("/mop")
    public List<MethodOfPaymentEnity> getMop() {
        return mopRepo.findAll();
    }

    @GetMapping("/products")
    public List<ProductEntity> getProducts() {
        return productRepo.findAll();
    }

    @GetMapping("/colors")
    public List<ColorEntity> getColors() {
        return colorRepo.findAll();
    }

    @GetMapping("/materials")
    public List<MaterialEntity> getMaterials() {
        return materialRepo.findAll();
    }

    @GetMapping("/dimensions")
    public List<DimensionsEntity> getDimensions() {
        return dimensionsRepo.findAll();
    }

    @GetMapping("/ship-method")
    public List<ShipMethodEntity> getSm() {
        return smRepo.findAll();
    }

    @PostMapping("/create")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
            if (userId == null) return ResponseEntity.badRequest().body("Thiếu thông tin người dùng");

            // 1. Create Order Entity
            OderEntity order = new OderEntity();
            order.setCustomerName((String) payload.get("customerName"));
            order.setEmail((String) payload.get("email"));
            order.setGender((String) payload.get("gender"));
            order.setPhone((String) payload.get("phone"));
            order.setAddressO((String) payload.get("addressO"));
            order.setNoteO((String) payload.get("noteO"));
            order.setReceiverName((String) payload.get("receiverName"));
            order.setReceiverPhone((String) payload.get("receiverPhone"));

            // Methods
            Map<String, Object> mopMap = (Map<String, Object>) payload.get("methodofPayment");
            if (mopMap != null && mopMap.get("id") != null) {
                mopRepo.findById(Long.valueOf(mopMap.get("id").toString())).ifPresent(order::setMethodofPayment);
            }

            Map<String, Object> smMap = (Map<String, Object>) payload.get("shipMethod");
            if (smMap != null && smMap.get("id") != null) {
                smRepo.findById(Long.valueOf(smMap.get("id").toString())).ifPresent(order::setShipMethod);
            }

            order = orderRepo.save(order);

            // 2. Create Bill Entity
            BillEntity bill = new BillEntity();
            bill.setOrderbill(order);
            bill.setCreateDate(new Date());
            bill.setTotalMoney(Math.round(Double.valueOf(payload.get("totalMoney").toString())));
            bill.setTotalMoneyaftersaleoff(Math.round(Double.valueOf(payload.get("totalMoneyaftersaleoff").toString())));
            
            // Set default status (Processing = 1)
            billStatusRepo.findById(1L).ifPresent(bill::setBill);
            billRepo.save(bill);

            // 3. Move items from Cart to OrderDetail and then DELETE from Cart
            List<CartDetailEntity> cartItems = cartDetailRepo.findActiveItemsByUserId(userId);
            for (CartDetailEntity item : cartItems) {
                OderDetailEntity od = new OderDetailEntity();
                od.setOrder(order);
                od.setProduct(item.getProductcartdetail());
                od.setAmount(item.getAmountCD());
                od.setPrice(item.getProductcartdetail().getPrice()); 
                
                // Gán màu sắc bằng đối tượng ColorEntity
                if (item.getIdColor() != null) {
                    colorRepo.findById(item.getIdColor()).ifPresent(od::setColor);
                }
                
                od.setMaterial(item.getMaterialcartdetail());
                od.setDimensions(item.getDemensionsCartDetail());
                orderDetailRepo.save(od);
                
                // XÓA VẬT LÝ để giỏ hàng chắc chắn trống
                cartDetailRepo.delete(item);
            }

            // 4. Update Cart Status to "Checkout Success" 
            List<ShopcartEntity> userCarts = shopcartRepo.findByUserSC_Id(userId);
            CartStatusEntity doneStatus = cartStatusRepo.findById(4L).orElse(null);
            if (doneStatus != null) {
                for (ShopcartEntity cart : userCarts) {
                    if (cart.getCartStatus() != null && cart.getCartStatus().getId() != 4L) {
                        cart.setCartStatus(doneStatus);
                        shopcartRepo.save(cart);
                    }
                }
            }

            return ResponseEntity.ok("Đặt hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/admin/update/{id}")
    @Transactional
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            OderEntity order = orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            if (payload.containsKey("customerName")) order.setCustomerName(payload.get("customerName").toString());
            if (payload.containsKey("phone")) order.setPhone(payload.get("phone").toString());
            if (payload.containsKey("email")) order.setEmail(payload.get("email").toString());
            if (payload.containsKey("addressO")) order.setAddressO(payload.get("addressO").toString());
            
            if (payload.containsKey("receiverName")) order.setReceiverName(payload.get("receiverName") != null ? payload.get("receiverName").toString() : null);
            if (payload.containsKey("receiverPhone")) order.setReceiverPhone(payload.get("receiverPhone") != null ? payload.get("receiverPhone").toString() : null);

            orderRepo.save(order);
            return ResponseEntity.ok("Cập nhật đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/delete/{id}")
    @Transactional
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            // 1. Xóa các hóa đơn (Bill) liên quan
            billRepo.deleteByOrderId(id);
            
            // 2. Xóa các chi tiết đơn hàng (OrderDetail)
            orderDetailRepo.deleteByOrderId(id);
            
            // 3. Xóa chính đơn hàng
            orderRepo.deleteById(id);
            
            return ResponseEntity.ok("Đã xóa đơn hàng và các dữ liệu liên quan thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi xóa đơn hàng: " + e.getMessage());
        }
    }

    @PostMapping("/admin/update-item/{id}")
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> updateOrderDetail(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            OderDetailEntity item = orderDetailRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm"));
            
            if (payload.containsKey("amount")) item.setAmount(Long.valueOf(payload.get("amount").toString()));
            if (payload.containsKey("price")) item.setPrice(Long.valueOf(payload.get("price").toString()));
            
            if (payload.get("product") != null) {
                Map<String, Object> prodMap = (Map<String, Object>) payload.get("product");
                productRepo.findById(Long.valueOf(prodMap.get("id").toString())).ifPresent(item::setProduct);
            }

            if (payload.get("color") != null) {
                Map<String, Object> colorMap = (Map<String, Object>) payload.get("color");
                colorRepo.findById(Long.valueOf(colorMap.get("id").toString())).ifPresent(item::setColor);
            }
            
            if (payload.get("material") != null) {
                Map<String, Object> matMap = (Map<String, Object>) payload.get("material");
                materialRepo.findById(Long.valueOf(matMap.get("id").toString())).ifPresent(item::setMaterial);
            }
            
            if (payload.get("dimensions") != null) {
                Map<String, Object> dimMap = (Map<String, Object>) payload.get("dimensions");
                dimensionsRepo.findById(Long.valueOf(dimMap.get("id").toString())).ifPresent(item::setDimensions);
            }

            orderDetailRepo.save(item);
            return ResponseEntity.ok("Cập nhật sản phẩm trong đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    public PageResponse<OderEntity> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return PageResponse.of(orderRepo.findWithSearch(search, 
                PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/admin/detail/{orderId}")
    public List<OderDetailEntity> getOrderItems(@PathVariable Long orderId) {
        return orderDetailRepo.findByOrderId(orderId);
    }
}
