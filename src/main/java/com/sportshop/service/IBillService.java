package com.sportshop.service;

import com.sportshop.dto.BillDTO;
import java.util.List;

public interface IBillService {
    List<BillDTO> getAll();
    BillDTO get(Long id);
}
