package com.sportshop.service;

import com.sportshop.entity.ProductEntity;
import com.sportshop.repository.ProductRepository;
import com.sportshop.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductInventoryService {

    public static final int LOW_STOCK_THRESHOLD = 10;

    @Autowired
    private ProductRepository productRepo;

    public PageResponse<ProductEntity> getInventory(int page, int size, String sort, String search, boolean lowStockOnly) {
        List<ProductEntity> filtered = productRepo.findAll().stream()
                .filter(p -> matchesSearch(p, search))
                .filter(p -> !lowStockOnly || parseStock(p.getAmount()) <= LOW_STOCK_THRESHOLD)
                .sorted(buildComparator(sort))
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<ProductEntity> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        Page<ProductEntity> result = new PageImpl<>(content, PageRequest.of(page, size), filtered.size());
        return PageResponse.of(result);
    }

    public long countLowStock() {
        return productRepo.findAll().stream()
                .filter(p -> parseStock(p.getAmount()) <= LOW_STOCK_THRESHOLD)
                .count();
    }

    @Transactional
    public ProductEntity updateStock(Long id, long newAmount) {
        if (newAmount < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }
        ProductEntity product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID " + id));
        product.setAmount(String.valueOf(newAmount));
        product.setUpdatedAt(new Date());
        return productRepo.save(product);
    }

    private boolean matchesSearch(ProductEntity p, String search) {
        if (search == null || search.isBlank()) return true;
        String s = search.trim().toLowerCase();
        boolean nameMatch = p.getName() != null && p.getName().toLowerCase().contains(s);
        boolean catMatch = p.getCategory() != null && p.getCategory().getName() != null
                && p.getCategory().getName().toLowerCase().contains(s);
        return nameMatch || catMatch;
    }

    private Comparator<ProductEntity> buildComparator(String sort) {
        if ("oldest".equals(sort)) {
            return Comparator.comparing(ProductEntity::getId);
        }
        if ("az".equals(sort)) {
            return Comparator.comparing(p -> p.getName() != null ? p.getName() : "", String.CASE_INSENSITIVE_ORDER);
        }
        if ("stock_asc".equals(sort)) {
            return Comparator.comparingLong(p -> parseStock(p.getAmount()));
        }
        if ("stock_desc".equals(sort)) {
            return Comparator.comparingLong((ProductEntity p) -> parseStock(p.getAmount())).reversed();
        }
        return Comparator.comparing(ProductEntity::getId).reversed();
    }

    public long parseStock(String amount) {
        if (amount == null || amount.isBlank()) return 0L;
        try {
            return Long.parseLong(amount.trim().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
