package com.sportshop.repository;

import com.sportshop.entity.VoucherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {

    @Query("SELECT v FROM VoucherEntity v " +
            "WHERE (:search IS NULL OR v.maVoucher LIKE %:search% OR CAST(v.id AS string) LIKE %:search%)")
    Page<VoucherEntity> findWithSearch(@Param("search") String search, Pageable pageable);

    boolean existsByMaVoucher(String maVoucher);

    Optional<VoucherEntity> findFirstByMaVoucher(String maVoucher);
}
