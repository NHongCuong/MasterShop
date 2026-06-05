package com.sportshop.service.impl;

import com.sportshop.dto.VoucherDTO;
import com.sportshop.entity.VoucherEntity;
import com.sportshop.repository.VoucherRepository;
import com.sportshop.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherService implements IVoucherService {

    @Autowired
    private VoucherRepository voucherRepo;

    private VoucherDTO toDTO(VoucherEntity e) {
        if (e == null) return null;
        VoucherDTO dto = new VoucherDTO();
        dto.setId(e.getId());
        dto.setMaVoucher(e.getMaVoucher());
        dto.setDiscountPercent(e.getDiscountPercent());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    private VoucherEntity toEntity(VoucherDTO dto) {
        VoucherEntity e = new VoucherEntity();
        e.setMaVoucher(dto.getMaVoucher());
        e.setDiscountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : 0);
        return e;
    }

    @Override
    public List<VoucherDTO> getAll() {
        return voucherRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Page<VoucherDTO> findAll(String search, Pageable pageable) {
        return voucherRepo.findWithSearch(search, pageable).map(this::toDTO);
    }

    @Override
    public VoucherDTO get(Long id) {
        return toDTO(voucherRepo.findById(id).orElse(null));
    }

    @Override
    public VoucherDTO getByCode(String maVoucher) {
        return toDTO(voucherRepo.findByMaVoucher(maVoucher).orElse(null));
    }

    @Override
    public void save(VoucherDTO dto) {
        if (voucherRepo.existsByMaVoucher(dto.getMaVoucher())) {
            throw new RuntimeException("Mã voucher đã tồn tại");
        }
        voucherRepo.save(toEntity(dto));
    }

    @Override
    public void update(Long id, VoucherDTO dto) {
        VoucherEntity old = voucherRepo.findById(id).orElse(null);
        if (old != null) {
            if (!old.getMaVoucher().equals(dto.getMaVoucher())) {
                if (voucherRepo.existsByMaVoucher(dto.getMaVoucher())) {
                    throw new RuntimeException("Mã voucher đã tồn tại");
                }
            }
            old.setMaVoucher(dto.getMaVoucher());
            old.setDiscountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : 0);
            voucherRepo.save(old);
        }
    }

    @Override
    public void delete(Long id) {
        voucherRepo.deleteById(id);
    }
}
