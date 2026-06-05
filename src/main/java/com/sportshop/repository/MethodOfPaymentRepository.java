package com.sportshop.repository;

import com.sportshop.entity.MethodOfPaymentEnity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MethodOfPaymentRepository extends JpaRepository<MethodOfPaymentEnity, Long> {
}
