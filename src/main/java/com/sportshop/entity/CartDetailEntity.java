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
    private ShopcartEntity shopcartdetail;

    @MapsId("idProduct")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_Product")
    private ProductEntity productcartdetail;

    // ⚠️ các quan hệ này có thể NULL
    //@MapsId("idMaterial")
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_Material",nullable = true)
    private MaterialEntity materialcartdetail;

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

    public ShopcartEntity getShopCartDetail() { return shopcartdetail; }
    public void setShopCartDetail(ShopcartEntity shopcartdetail) { this.shopcartdetail = shopcartdetail; }

    public ProductEntity getProductCartDetail() { return productcartdetail; }
    public void setProductCartDetail(ProductEntity productcartdetail) { this.productcartdetail = productcartdetail; }

    public MaterialEntity getMaterialCartDetail() { return materialcartdetail; }
    public void setMaterialCartDetail(MaterialEntity cartDetail) { this.materialcartdetail = materialcartdetail; }

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
