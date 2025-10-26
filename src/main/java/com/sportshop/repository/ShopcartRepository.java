package com.sportshop.repository;

import com.sportshop.entity.ShopcartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopcartRepository extends JpaRepository<ShopcartEntity, Long > {
}
