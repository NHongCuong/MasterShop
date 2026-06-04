package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "material")
public class MaterialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Material")
    private Long id;

    @Column(name = "Name_Material")
    private String nameMaterial;

    @Column(name = "created_at")
    public java.util.Date createdAt;

    @Column(name = "updated_at")
    public java.util.Date updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new java.util.Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }

    @OneToMany(mappedBy = "materialcartdetail")
    @JsonIgnore
    private List<CartDetailEntity> material;

    @OneToMany(mappedBy = "detailMaterial")
    @JsonIgnore
    private List<DetailProductMaterialEntity> detailproductmaterial;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameMaterial() {
        return nameMaterial;
    }

    public void setNameMaterial(String nameMaterial) {
        this.nameMaterial = nameMaterial;
    }

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.util.Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CartDetailEntity> getMaterial() {
        return material;
    }

    public void setMaterial(List<CartDetailEntity> material) {
        this.material = material;
    }

    public List<DetailProductMaterialEntity> getDetailproductmaterial() {
        return detailproductmaterial;
    }

    public void setDetailproductmaterial(List<DetailProductMaterialEntity> detailproductmaterial) {
        this.detailproductmaterial = detailproductmaterial;
    }

}
