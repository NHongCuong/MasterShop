package com.sportshop.entity;

import javax.persistence.*;

@Entity
@Table(name="order_detail")
public class OderDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Order")
    private Long id;

    @ManyToOne
    @JoinColumn(name="ID_SC")
    public ShopcartEntity  shopcartOD;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShopcartEntity getShopcartOD() {
        return shopcartOD;
    }

    public void setShopcartOD(ShopcartEntity shopcartOD) {
        this.shopcartOD = shopcartOD;
    }

}
