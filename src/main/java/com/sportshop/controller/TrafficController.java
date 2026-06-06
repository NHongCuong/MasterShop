package com.sportshop.controller;

import com.sportshop.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/traffic")
public class TrafficController {

    @Autowired
    private TrafficService trafficService;

    @PostMapping("/visit")
    public ResponseEntity<?> recordVisit() {
        try {
            trafficService.recordVisit();
            return ResponseEntity.ok(Map.of("message", "Đã ghi nhận lượt truy cập"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Lỗi ghi nhận lượt truy cập: " + e.getMessage()));
        }
    }

    @GetMapping("/total")
    public ResponseEntity<?> getTotal() {
        return ResponseEntity.ok(Map.of("totalVisits", trafficService.getTotalVisits()));
    }
}
