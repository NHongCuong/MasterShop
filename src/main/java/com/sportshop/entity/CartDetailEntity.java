package com.sportshop.entity;


import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="cart_detail")
public class CartDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CD")
    public Long id;

    @ManyToOne
    @JoinColumn(name="ID_Product")
    public ProductEntity orderDetail;

    @ManyToOne
    @JoinColumn(name="ID_Material")
    public MaterialEntity cartDetail;

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ManyToOne
    @JoinColumn(name="ID_SC")
    public ShopcartEntity cartsDetail;


    @ManyToOne
    @JoinColumn(name="ID_D")
    public DimensionsEntity demensionsCartDetail;

    @Column(name="Amount_CD")
    public Long AmountCD;

    @Column(name="created_at")
    public Date created_at;

    @Column(name="updated_at")
    public Date updated_at;

    @Column(name="ID_Color")
    public Long IDColor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductEntity getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(ProductEntity orderDetail) {
        this.orderDetail = orderDetail;
    }

    public MaterialEntity getCartDetail() {
        return cartDetail;
    }

    public void setCartDetail(MaterialEntity cartDetail) {
        this.cartDetail = cartDetail;
    }

    public ShopcartEntity getCartsDetail() {
        return cartsDetail;
    }

    public void setCartsDetail(ShopcartEntity cartsDetail) {
        this.cartsDetail = cartsDetail;
    }

    public DimensionsEntity getDemensionsCartDetail() {
        return demensionsCartDetail;
    }

    public void setDemensionsCartDetail(DimensionsEntity demensionsCartDetail) {
        this.demensionsCartDetail = demensionsCartDetail;
    }

    public Long getAmountCD() {
        return AmountCD;
    }

    public void setAmountCD(Long amountCD) {
        AmountCD = amountCD;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public Long getIDColor() {
        return IDColor;
    }

    public void setIDColor(Long IDColor) {
        this.IDColor = IDColor;
    }



}
