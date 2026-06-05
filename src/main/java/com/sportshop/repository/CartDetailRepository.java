package com.sportshop.repository;

import com.sportshop.entity.CartDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetailEntity, Long> {

    @Query("SELECT cd FROM CartDetailEntity cd " +
           "WHERE cd.shopcartdetail.id = :scId " +
           "AND cd.productcartdetail.id = :prodId " +
           "AND (:colorId IS NULL OR cd.colorEntity.id = :colorId) " +
           "AND (:matId IS NULL OR cd.materialcartdetail.id = :matId) " +
           "AND (:dimId IS NULL OR cd.demensionsCartDetail.Id = :dimId)")
    java.util.Optional<CartDetailEntity> findByAllAttributes(
            Long scId, Long prodId, Long colorId, Long matId, Long dimId);

    @Query("SELECT cd FROM CartDetailEntity cd " +
            "LEFT JOIN FETCH cd.shopcartdetail " +
            "LEFT JOIN FETCH cd.productcartdetail " +
            "LEFT JOIN FETCH cd.materialcartdetail " +
            "LEFT JOIN FETCH cd.demensionsCartDetail " +
            "LEFT JOIN FETCH cd.colorEntity")
    List<CartDetailEntity> findAllIncludeNulls();

    @Query("SELECT cd FROM CartDetailEntity cd " +
            "LEFT JOIN FETCH cd.shopcartdetail sc " +
            "LEFT JOIN FETCH cd.productcartdetail " +
            "LEFT JOIN FETCH cd.materialcartdetail " +
            "LEFT JOIN FETCH cd.demensionsCartDetail " +
            "LEFT JOIN FETCH cd.colorEntity " +
            "WHERE sc.userSC.id = :userId AND sc.cartStatus.id = 3")
    List<CartDetailEntity> findActiveItemsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
