package com.sportshop.repository;

import com.sportshop.entity.OderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OderDetailRepository extends JpaRepository<OderDetailEntity, Long> {
    @Query("SELECT o FROM OderDetailEntity o WHERE o.order.id = :orderId")
    List<OderDetailEntity> findByOrderId(Long orderId);

    @Modifying
    @Query("DELETE FROM OderDetailEntity o WHERE o.order.id = :orderId")
    void deleteByOrderId(Long orderId);
}
