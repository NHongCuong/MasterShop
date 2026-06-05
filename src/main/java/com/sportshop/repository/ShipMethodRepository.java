package com.sportshop.repository;

import com.sportshop.entity.ShipMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipMethodRepository extends JpaRepository<ShipMethodEntity, Long> {
}
