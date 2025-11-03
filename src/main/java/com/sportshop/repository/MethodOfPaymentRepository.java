package com.sportshop.repository;

import com.sportshop.entity.MethodOfPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MethodOfPaymentRepository extends JpaRepository<MethodOfPaymentEntity,Long> {
}
