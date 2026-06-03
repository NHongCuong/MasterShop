package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="ship_method")
public class ShipMethodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SM")
    public Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<OderEntity> getOrders() {
        return orders;
    }

    public void setOrders(List<OderEntity> orders) {
        this.orders = orders;
    }

    @OneToMany(mappedBy="shipMethod")
    @JsonIgnore
    private List<OderEntity> orders;
}
