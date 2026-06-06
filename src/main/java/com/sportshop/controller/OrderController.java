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

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    @Autowired
    private VoucherRepository voucherRepo;

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
    @Transactional
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

            // 2. Determine Items to Order (From Payload or Database Cart)
            long totalMoney = 0;
            long totalAfterSale = 0;
            String voucherCode = (String) payload.get("voucherCode");
            VoucherEntity appliedVoucher = (voucherCode != null && !voucherCode.trim().isEmpty()) 
                    ? voucherRepo.findByMaVoucher(voucherCode.trim()).orElse(null) : null;

            if (payload.containsKey("cartItems") && payload.get("cartItems") instanceof List) {
                // Use items from payload (for Buy Now or Selective Checkout)
                List<Map<String, Object>> itemsPayload = (List<Map<String, Object>>) payload.get("cartItems");
                for (Map<String, Object> itemMap : itemsPayload) {
                    Long productId = Long.valueOf(itemMap.get("idProduct").toString());
                    ProductEntity product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + productId));
                    long amount = Long.valueOf(itemMap.get("amountCD").toString());
                    long basePrice = Long.valueOf(itemMap.get("productPrice").toString());
                    
                    // ✅ Áp dụng giảm giá trực tiếp của sản phẩm
                    long discountedPrice = basePrice;
                    if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0) {
                        discountedPrice = Math.round(basePrice * (1 - product.getDiscountPercent() / 100.0));
                    }

                    OderDetailEntity od = new OderDetailEntity();
                    od.setOrder(order);
                    od.setProduct(product);
                    od.setAmount(amount);
                    od.setPrice(discountedPrice);

                    // Add details if present
                    if (itemMap.get("colorId") != null) {
                        colorRepo.findById(Long.valueOf(itemMap.get("colorId").toString())).ifPresent(od::setColor);
                    }
                    if (itemMap.get("materialId") != null) {
                        materialRepo.findById(Long.valueOf(itemMap.get("materialId").toString())).ifPresent(od::setMaterial);
                    }
                    if (itemMap.get("dimensionId") != null) {
                        dimensionsRepo.findById(Long.valueOf(itemMap.get("dimensionId").toString())).ifPresent(od::setDimensions);
                    }

                    orderDetailRepo.save(od);

                    long itemTotal = discountedPrice * amount;
                    totalMoney += (basePrice * amount); // Lưu tổng tiền gốc để tính discount tổng
                    
                    // Calc sale off (Voucher overlay)
                    if (appliedVoucher != null && product.getVoucher() != null && 
                        product.getVoucher().getId().equals(appliedVoucher.getId())) {
                        long voucherDiscount = Math.round(itemTotal * appliedVoucher.getDiscountPercent() / 100.0);
                        totalAfterSale += (itemTotal - voucherDiscount);
                    } else {
                        totalAfterSale += itemTotal;
                    }
                    
                    // CLEANUP: If this item was in the DB cart, remove it
                    cartDetailRepo.findActiveItemsByUserId(userId).stream()
                        .filter(cd -> cd.getProductcartdetail().getId().equals(productId))
                        .forEach(cartDetailRepo::delete);
                }
            } else {
                // Fallback to traditional DB-based Cart Checkout
                List<CartDetailEntity> cartItems = cartDetailRepo.findActiveItemsByUserId(userId);
                for (CartDetailEntity item : cartItems) {
                    ProductEntity product = item.getProductcartdetail();
                    long basePrice = product.getPrice();
                    long discountedPrice = basePrice;
                    if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0) {
                        discountedPrice = Math.round(basePrice * (1 - product.getDiscountPercent() / 100.0));
                    }

                    long itemTotal = discountedPrice * item.getAmountCD();
                    totalMoney += (basePrice * item.getAmountCD());

                    if (appliedVoucher != null && product.getVoucher() != null && 
                        product.getVoucher().getId().equals(appliedVoucher.getId())) {
                        long voucherDiscount = Math.round(itemTotal * appliedVoucher.getDiscountPercent() / 100.0);
                        totalAfterSale += (itemTotal - voucherDiscount);
                    } else {
                        totalAfterSale += itemTotal;
                    }

                    OderDetailEntity od = new OderDetailEntity();
                    od.setOrder(order);
                    od.setProduct(product);
                    od.setAmount(item.getAmountCD());
                    od.setPrice(discountedPrice);
                    if (item.getColorEntity() != null) od.setColor(item.getColorEntity());
                    od.setMaterial(item.getMaterialcartdetail());
                    od.setDimensions(item.getDemensionsCartDetail());
                    orderDetailRepo.save(od);
                    cartDetailRepo.delete(item);
                }
            }

            // 3. Create Bill Entity
            BillEntity bill = new BillEntity();
            bill.setOrderbill(order);
            bill.setCreateDate(new Date());
            bill.setTotalMoney(totalMoney);
            bill.setTotalMoneyaftersaleoff(totalAfterSale);
            bill.setDiscount(totalMoney - totalAfterSale);
            billStatusRepo.findById(1L).ifPresent(bill::setBill);
            billRepo.save(bill);

            // 4. Update Cart Status
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

            order.setUpdatedDate(new java.util.Date());
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

            // Cập nhật ngày sửa của đơn hàng cha
            OderEntity orderItem = item.getOrder();
            if (orderItem != null) {
                orderItem.setUpdatedDate(new java.util.Date());
                orderRepo.save(orderItem);
            }

            // TÍNH LẠI TỔNG TIỀN CHO BILL
            Long orderId = item.getOrder().getId();
            List<OderDetailEntity> allItems = orderDetailRepo.findByOrderId(orderId);
            long subTotal = 0;
            for (OderDetailEntity od : allItems) {
                subTotal += (od.getAmount() * od.getPrice());
            }

            List<BillEntity> bills = billRepo.findByOrderbill_Id(orderId);
            if (!bills.isEmpty()) {
                BillEntity bill = bills.get(0);
                bill.setTotalMoney(subTotal);
                
                // Nếu có discount thì trừ đi
                long discount = bill.getDiscount() != null ? bill.getDiscount() : 0;
                bill.setTotalMoneyaftersaleoff(subTotal - discount);
                
                billRepo.save(bill);
            }

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

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=orders_detail.xlsx";
        response.setHeader(headerKey, headerValue);

        List<OderEntity> listOrders = orderRepo.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Order Details");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID Đơn", "Khách hàng", "SĐT", "Địa chỉ", 
            "Người nhận khác", "SĐT Người nhận", 
            "Sản phẩm & Thuộc tính", "Số lượng", "Đơn giá", "Thành tiền",
            "Giảm giá (Đơn)", "Tổng thanh toán",
            "Thanh toán", "Vận chuyển", "Ngày tạo", "Ngày sửa"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Cell styles for data
        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        currencyStyle.setDataFormat(df.getFormat("#,##0"));
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(df.getFormat("dd/MM/yyyy HH:mm:ss"));
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle normalBorder = workbook.createCellStyle();
        normalBorder.setBorderBottom(BorderStyle.THIN);
        normalBorder.setBorderTop(BorderStyle.THIN);
        normalBorder.setBorderRight(BorderStyle.THIN);
        normalBorder.setBorderLeft(BorderStyle.THIN);

        int rowCount = 1;
        for (OderEntity order : listOrders) {
            List<OderDetailEntity> items = orderDetailRepo.findByOrderId(order.getId());
            
            // Lấy thông tin Bill để lấy discount và final total
            List<BillEntity> bills = billRepo.findByOrderbill_Id(order.getId());
            long orderDiscount = 0;
            long orderFinalTotal = 0;
            if (!bills.isEmpty()) {
                orderDiscount = bills.get(0).getDiscount() != null ? bills.get(0).getDiscount() : 0;
                orderFinalTotal = bills.get(0).getTotalMoneyaftersaleoff() != null ? bills.get(0).getTotalMoneyaftersaleoff() : 0;
            }

            for (OderDetailEntity item : items) {
                Row row = sheet.createRow(rowCount++);
                
                Cell c0 = row.createCell(0); c0.setCellValue(order.getId()); c0.setCellStyle(normalBorder);
                Cell c1 = row.createCell(1); c1.setCellValue(order.getCustomerName()); c1.setCellStyle(normalBorder);
                Cell c2 = row.createCell(2); c2.setCellValue(order.getPhone()); c2.setCellStyle(normalBorder);
                Cell c3 = row.createCell(3); c3.setCellValue(order.getAddressO()); c3.setCellStyle(normalBorder);
                
                Cell c4 = row.createCell(4); c4.setCellValue(order.getReceiverName() != null ? order.getReceiverName() : ""); c4.setCellStyle(normalBorder);
                Cell c5 = row.createCell(5); c5.setCellValue(order.getReceiverPhone() != null ? order.getReceiverPhone() : ""); c5.setCellStyle(normalBorder);
                
                // Sản phẩm & Thuộc tính
                StringBuilder prodInfo = new StringBuilder(item.getProduct().getName());
                if (item.getColor() != null || item.getMaterial() != null || item.getDimensions() != null) {
                    prodInfo.append(" (");
                    boolean first = true;
                    if (item.getColor() != null) {
                        prodInfo.append("Màu: ").append(item.getColor().getNameColor());
                        first = false;
                    }
                    if (item.getMaterial() != null) {
                        if (!first) prodInfo.append(", ");
                        prodInfo.append("Chất liệu: ").append(item.getMaterial().getNameMaterial());
                        first = false;
                    }
                    if (item.getDimensions() != null) {
                        if (!first) prodInfo.append(", ");
                        prodInfo.append("Kích cỡ: ").append(item.getDimensions().getNameD());
                    }
                    prodInfo.append(")");
                }
                Cell c6 = row.createCell(6); c6.setCellValue(prodInfo.toString()); c6.setCellStyle(normalBorder);
                
                Cell c7 = row.createCell(7); c7.setCellValue(item.getAmount()); c7.setCellStyle(normalBorder);
                
                // Tiền tệ
                Cell c8 = row.createCell(8); c8.setCellValue(item.getPrice()); c8.setCellStyle(currencyStyle);
                Cell c9 = row.createCell(9); c9.setCellValue(item.getAmount() * item.getPrice()); c9.setCellStyle(currencyStyle);
                Cell c10 = row.createCell(10); c10.setCellValue(orderDiscount); c10.setCellStyle(currencyStyle);
                Cell c11 = row.createCell(11); c11.setCellValue(orderFinalTotal); c11.setCellStyle(currencyStyle);
                
                Cell c12 = row.createCell(12); c12.setCellValue(order.getMethodofPayment() != null ? order.getMethodofPayment().getName_mop() : "N/A"); c12.setCellStyle(normalBorder);
                Cell c13 = row.createCell(13); c13.setCellValue(order.getShipMethod() != null ? order.getShipMethod().getNameSM() : "N/A"); c13.setCellStyle(normalBorder);
                
                // Ngày tháng
                Cell c14 = row.createCell(14); 
                if (order.getCreatedDate() != null) { c14.setCellValue(order.getCreatedDate()); c14.setCellStyle(dateStyle); } 
                else { c14.setCellStyle(normalBorder); }
                
                Cell c15 = row.createCell(15);
                if (order.getUpdatedDate() != null) { c15.setCellValue(order.getUpdatedDate()); c15.setCellStyle(dateStyle); }
                else { c15.setCellStyle(normalBorder); }
            }
        }

        // Auto size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
