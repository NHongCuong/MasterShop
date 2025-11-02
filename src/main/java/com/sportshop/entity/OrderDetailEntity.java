package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name="order_detail")
public class OrderDetailEntity {

    @EmbeddedId
    private OrderDetailId id;

    @MapsId("idOrder")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="ID_Order")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public OrderEntity orderEntity;

    @MapsId("idSC")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="ID_SC")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public ShopcartEntity  shopcartOD;

    public OrderDetailId getId() {
        return id;
    }

    public void setId(OrderDetailId id) {
        this.id = id;
    }

    public OrderEntity getOrderEntity() {
        return orderEntity;
    }

    public void setOrderEntity(OrderEntity orderEntity) {
        this.orderEntity = orderEntity;
    }

    public ShopcartEntity getShopcartOD() {
        return shopcartOD;
    }

    public void setShopcartOD(ShopcartEntity shopcartOD) {
        this.shopcartOD = shopcartOD;
    }




}
