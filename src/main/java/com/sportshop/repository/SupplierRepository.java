package com.sportshop.repository;

import com.sportshop.entity.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long>{
    @Query("SELECT s FROM SupplierEntity s WHERE lower(s.name) LIKE lower(concat('%', :search, '%')) " +
           "OR lower(s.email) LIKE lower(concat('%', :search, '%')) " +
           "OR lower(s.phone) LIKE lower(concat('%', :search, '%'))")
    Page<SupplierEntity> findBySearch(String search, Pageable pageable);

    Optional<SupplierEntity> findByName(String name);
}
