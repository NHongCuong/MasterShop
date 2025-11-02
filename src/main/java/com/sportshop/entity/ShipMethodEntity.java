package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="ship_method")
public class ShipMethodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SM")
    public Long id;

    @Column(name = "Name_SM")
    public String nameSM;

    @OneToMany(mappedBy="shipMethod")
    @JsonIgnore
    private List<OrderEntity> orders;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<OrderEntity> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderEntity> orders) {
        this.orders = orders;
    }

    public String getNameSM() {
        return nameSM;
    }

    public void setNameSM(String nameSM) {
        this.nameSM = nameSM;
    }





}
