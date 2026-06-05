package com.sportshop.repository;

import com.sportshop.entity.BillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long> {
    List<BillEntity> findByOrderbill_Id(Long orderId);

    @Modifying
    @Query("DELETE FROM BillEntity b WHERE b.orderbill.id = :orderId")
    void deleteByOrderId(Long orderId);
}
