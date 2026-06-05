package com.sportshop.service;

import com.sportshop.dto.VoucherDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IVoucherService {
    List<VoucherDTO> getAll();
    Page<VoucherDTO> findAll(String search, Pageable pageable);
    VoucherDTO get(Long id);
    VoucherDTO getByCode(String maVoucher);
    void save(VoucherDTO dto);
    void update(Long id, VoucherDTO dto);
    void delete(Long id);
}
