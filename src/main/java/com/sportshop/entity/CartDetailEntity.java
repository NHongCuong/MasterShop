package com.sportshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cart_detail")
public class CartDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cart_detail")
    private Long idCartDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SC")
    private ShopcartEntity shopcartdetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Product")
    private ProductEntity productcartdetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Material")
    private MaterialEntity materialcartdetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_D")
    private DimensionsEntity demensionsCartDetail;

    @Column(name = "Amount_CD")
    private Long amountCD;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Color")
    private ColorEntity colorEntity;
}
