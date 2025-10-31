package com.sportshop.repository;

import com.sportshop.entity.DimensionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DimensionsRepository extends JpaRepository<DimensionsEntity, Long> {
    List<DimensionsEntity> findByDemensions_Id(Long idProduct);
}
