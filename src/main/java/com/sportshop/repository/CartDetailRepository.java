package com.sportshop.repository;

import com.sportshop.entity.CartDetailEntity;
import com.sportshop.entity.CartDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetailEntity, CartDetailId> {

    @Query("SELECT cd FROM CartDetailEntity cd " +
            "LEFT JOIN FETCH cd.shopcartdetail " +
            "LEFT JOIN FETCH cd.productcartdetail " +
            "LEFT JOIN FETCH cd.materialcartdetail " +
            "LEFT JOIN FETCH cd.demensionsCartDetail ")
    List<CartDetailEntity> findAllIncludeNulls();

    @Query("SELECT cd FROM CartDetailEntity cd " +
            "LEFT JOIN FETCH cd.shopcartdetail sc " +
            "LEFT JOIN FETCH cd.productcartdetail " +
            "LEFT JOIN FETCH cd.materialcartdetail " +
            "LEFT JOIN FETCH cd.demensionsCartDetail " +
            "WHERE sc.userSC.id = :userId AND sc.cartStatus.id = 3")
    List<CartDetailEntity> findActiveItemsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
