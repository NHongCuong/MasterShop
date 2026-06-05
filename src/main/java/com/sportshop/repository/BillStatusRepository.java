package com.sportshop.repository;

import com.sportshop.entity.BillStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillStatusRepository extends JpaRepository<BillStatusEntity, Long> {
}
