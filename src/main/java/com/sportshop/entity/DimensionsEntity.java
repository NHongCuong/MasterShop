package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="dimensions")
public class DimensionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_D")
    private Long Id;

    @Column(name="Name_D")
    private String nameD;

    @Column(name="created_at")
    private Date created_at;

    @Column(name="updated_at")
    private Date updated_at;

    @ManyToOne
    @JoinColumn(name="ID_Product")
    public ProductEntity demensions;

    @OneToMany(mappedBy = "demensionsCartDetail")
    @JsonIgnore
    private List<CartDetailEntity> demensionsCartDetail;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getNameD() {
        return nameD;
    }

    public void setNameD(String nameD) {
        this.nameD = nameD;
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

    public ProductEntity getDemensions() {
        return demensions;
    }

    public void setDemensions(ProductEntity demensions) {
        this.demensions = demensions;
    }

}
