package com.sportshop.repository;

import com.sportshop.entity.BillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long> {
    List<BillEntity> findByOrderbill_Id(Long orderId);

    @Modifying
    @Query("DELETE FROM BillEntity b WHERE b.orderbill.id = :orderId")
    void deleteByOrderId(Long orderId);

    @Query("SELECT b FROM BillEntity b WHERE cast(b.id as string) LIKE %:search% " +
           "OR cast(b.orderbill.id as string) LIKE %:search% " +
           "OR lower(b.orderbill.customerName) LIKE lower(concat('%', :search, '%'))")
    Page<BillEntity> findBySearch(String search, Pageable pageable);

    @Query("SELECT b FROM BillEntity b WHERE b.orderbill.email = :email")
    Page<BillEntity> findByEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT b FROM BillEntity b WHERE (b.orderbill.user.id = :userId OR b.orderbill.email = :email)")
    Page<BillEntity> findByUserIdOrEmail(@Param("userId") Long userId, @Param("email") String email, Pageable pageable);
}
