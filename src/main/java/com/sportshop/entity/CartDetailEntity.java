package com.sportshop.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cart_detail")
public class CartDetailEntity {

    @EmbeddedId
    private CartDetailId id;

    @MapsId("idSC")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SC")
    private ShopcartEntity cartsDetail;

    @MapsId("idProduct")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_Product")
    private ProductEntity orderDetail;

    // ⚠️ các quan hệ này có thể NULL
    //@MapsId("idMaterial")
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_Material",nullable = true)
    private MaterialEntity cartDetail;

    //@MapsId("idD")
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_D",nullable = true)
    private DimensionsEntity demensionsCartDetail;

    @Column(name = "Amount_CD")
    private Long amountCD;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "ID_Color")
    private Long idColor;

    // Getters & Setters (giữ nguyên như cũ)
    public CartDetailId getId() { return id; }
    public void setId(CartDetailId id) { this.id = id; }

    public ShopcartEntity getCartsDetail() { return cartsDetail; }
    public void setCartsDetail(ShopcartEntity cartsDetail) { this.cartsDetail = cartsDetail; }

    public ProductEntity getOrderDetail() { return orderDetail; }
    public void setOrderDetail(ProductEntity orderDetail) { this.orderDetail = orderDetail; }

    public MaterialEntity getCartDetail() { return cartDetail; }
    public void setCartDetail(MaterialEntity cartDetail) { this.cartDetail = cartDetail; }

    public DimensionsEntity getDemensionsCartDetail() { return demensionsCartDetail; }
    public void setDemensionsCartDetail(DimensionsEntity demensionsCartDetail) { this.demensionsCartDetail = demensionsCartDetail; }

    public Long getAmountCD() { return amountCD; }
    public void setAmountCD(Long amountCD) { this.amountCD = amountCD; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Long getIdColor() { return idColor; }
    public void setIdColor(Long idColor) { this.idColor = idColor; }
}
