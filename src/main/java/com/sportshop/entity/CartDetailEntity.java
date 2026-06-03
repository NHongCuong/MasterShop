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

    @EmbeddedId
    private CartDetailId id;

    @MapsId("idSC")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SC")
    private ShopcartEntity shopcartdetail;

    @MapsId("idProduct")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_Product")
    private ProductEntity productcartdetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_Material", nullable = true)
    private MaterialEntity materialcartdetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_D", nullable = true)
    private DimensionsEntity demensionsCartDetail;

    @Column(name = "Amount_CD")
    private Long amountCD;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "ID_Color")
    private Long idColor;
}
