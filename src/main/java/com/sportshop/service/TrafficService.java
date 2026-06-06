package com.sportshop.service;

import com.sportshop.entity.TrafficEntity;
import com.sportshop.repository.TrafficRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TrafficService {

    @Autowired
    private TrafficRepository trafficRepo;

    @Transactional
    public void recordVisit() {
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        Date start = Date.from(today.atStartOfDay(zone).toInstant());
        Date end = Date.from(today.plusDays(1).atStartOfDay(zone).toInstant());

        TrafficEntity traffic = trafficRepo.findByCreateDateBetween(start, end)
                .orElseGet(() -> TrafficEntity.builder()
                        .count(0L)
                        .createDate(new Date())
                        .build());

        traffic.setCount(traffic.getCount() + 1);
        trafficRepo.save(traffic);
    }

    public long getTotalVisits() {
        return trafficRepo.sumTotalCount();
    }
}
