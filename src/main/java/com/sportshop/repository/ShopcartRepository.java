package com.sportshop.repository;

import com.sportshop.entity.ShopcartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopcartRepository extends JpaRepository<ShopcartEntity, Long > {
    java.util.List<ShopcartEntity> findByUserSC_IdAndCartStatus_Id(Long userId, Long statusId);
}
