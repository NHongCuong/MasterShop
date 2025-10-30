package com.sportshop.repository;

import com.sportshop.dto.DetailProductColorDTO;
import com.sportshop.entity.DetailProductColorEntity;
import com.sportshop.entity.DetailProductColorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailProductColorRepository extends JpaRepository<DetailProductColorEntity, DetailProductColorId> {
    // tìm theo idProduct trong embedded id
   // List<DetailProductColorEntity> findAllDTO();
    List<DetailProductColorEntity> findByIdIdProduct(Long idProduct);
}
