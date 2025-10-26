package com.sportshop.repository;

import com.sportshop.entity.CartStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartStatusRepository extends JpaRepository<CartStatusEntity,Long> {
}
