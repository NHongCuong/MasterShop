package com.sportshop.service;

import com.sportshop.entity.ProductEntity;
import com.sportshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductStockService {

    @Autowired
    private ProductRepository productRepo;

    /**
     * Cập nhật tồn kho sau khi đặt hàng thành công:
     * amount_product = tồn kho hiện tại - số lượng đặt hàng
     * sold_quantity  = đã bán + số lượng đặt hàng
     */
    @Transactional
    public void deductStockAfterOrder(Long productId, long orderQuantity) {
        if (productId == null || orderQuantity <= 0) return;

        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + productId));

        long currentStock = parseStockAmount(product.getAmount());
        if (currentStock < orderQuantity) {
            throw new RuntimeException(
                    "Sản phẩm \"" + product.getName() + "\" không đủ hàng trong kho (còn " + currentStock + ")");
        }

        long newStock = currentStock - orderQuantity;
        product.setAmount(String.valueOf(newStock));

        long sold = product.getSoldQuantity() != null ? product.getSoldQuantity() : 0L;
        product.setSoldQuantity(sold + orderQuantity);

        productRepo.saveAndFlush(product);
    }

    private long parseStockAmount(String amount) {
        if (amount == null || amount.isBlank()) return 0L;
        try {
            return Long.parseLong(amount.trim().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
