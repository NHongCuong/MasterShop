package com.sportshop.repository;

import com.sportshop.entity.OrderDetailEntity;
import com.sportshop.entity.OrderDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, OrderDetailId> {
    List<OrderDetailEntity> findById_IdSC(Long idSC);
}
