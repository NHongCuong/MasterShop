package com.sportshop.controller;

import com.sportshop.entity.BillEntity;
import com.sportshop.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/bill")
public class BillController {

    @Autowired
    private BillRepository billRepo;

    @GetMapping("/order/{orderId}")
    public List<BillEntity> getByOrderId(@PathVariable Long orderId) {
        return billRepo.findByOrderbill_Id(orderId);
    }
}
