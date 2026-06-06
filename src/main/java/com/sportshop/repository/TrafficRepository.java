package com.sportshop.repository;

import com.sportshop.entity.TrafficEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface TrafficRepository extends JpaRepository<TrafficEntity, Long> {

    @Query("SELECT t FROM TrafficEntity t WHERE t.createDate >= :start AND t.createDate < :end")
    Optional<TrafficEntity> findByCreateDateBetween(@Param("start") Date start, @Param("end") Date end);

    @Query("SELECT COALESCE(SUM(t.count), 0) FROM TrafficEntity t")
    long sumTotalCount();
}
