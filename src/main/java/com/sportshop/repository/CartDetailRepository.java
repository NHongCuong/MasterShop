package com.sportshop.repository;

import com.sportshop.entity.CartDetailEntity;
import com.sportshop.entity.CartDetailId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetailEntity, CartDetailId> {
//    @Override
//    @EntityGraph(attributePaths = {
//            "cartsDetail",
//            "orderDetail",
//            "cartDetail",
//            "demensionsCartDetail"
//    })
//    List<CartDetailEntity> findAll();
    @Query("SELECT cd FROM CartDetailEntity cd " +
            "LEFT JOIN FETCH cd.shopcartdetail " +
            "LEFT JOIN FETCH cd.productcartdetail " +
            "LEFT JOIN FETCH cd.materialcartdetail " +
            "LEFT JOIN FETCH cd.demensionsCartDetail ")
    List<CartDetailEntity> findAllIncludeNulls();


}
