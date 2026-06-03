package com.sportshop.service.impl;

import com.sportshop.converter.BillConverter;
import com.sportshop.dto.BillDTO;
import com.sportshop.entity.BillEntity;
import com.sportshop.repository.BillRepository;
import com.sportshop.service.IBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BillService implements IBillService {
    @Autowired
    BillRepository billRepo;
    @Autowired
    BillConverter billConverter;
    @Override
    public List<BillDTO> getAll(){
        List<BillEntity> list =  billRepo.findAll();
        List<BillDTO> result = new ArrayList<>();
        for(BillEntity en: list)
        {
            BillDTO dto = billConverter.toDTO(en);
            result.add(dto);
        }
        return result;
    }
    @Override
    public BillDTO get(Long id) {
        BillEntity bill = billRepo.findById(id).orElse(null);
        BillDTO dto = billConverter.toDTO(bill);
        return dto;
    }
}
