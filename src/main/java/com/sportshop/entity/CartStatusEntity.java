package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity

@Table(name = "cart_status")
public class CartStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CS")
    public Long id;

    @Column(name = "Name_CS")
    public String nameCS;

    @Column(name = "created_at")
    public Date created_at;
    @Column
    public Date updated_at;

    @OneToMany(mappedBy="cartStatus")
    @JsonIgnore
    private List<ShopcartEntity> shopcarts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameCS() {
        return nameCS;
    }

    public void setNameCS(String name_sc) {
        this.nameCS = name_sc;
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

    public List<ShopcartEntity> getShopcarts() {
        return shopcarts;
    }

    public void setShopcarts(List<ShopcartEntity> shopcarts) {
        this.shopcarts = shopcarts;
    }


}
