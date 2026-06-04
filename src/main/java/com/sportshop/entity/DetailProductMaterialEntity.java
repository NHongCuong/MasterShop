package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name="detail_product_material")
public class DetailProductMaterialEntity {
    @EmbeddedId
    private DetailProductMaterialId id;

    @MapsId("idMaterial")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_Material")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MaterialEntity detailMaterial;

    @MapsId("idProduct")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="ID_Product")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductEntity detailMaterialProduct;

    @Column(name = "created_at")
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    private java.util.Date updatedAt;

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

    public java.util.Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }

    public java.util.Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.util.Date updatedAt) { this.updatedAt = updatedAt; }

    public DetailProductMaterialId getId() {
        return id;
    }

    public void setId(DetailProductMaterialId id) {
        this.id = id;
    }

    public MaterialEntity getDetailMaterial() {
        return detailMaterial;
    }

    public void setDetailMaterial(MaterialEntity detailMaterial) {
        this.detailMaterial = detailMaterial;
    }

    public ProductEntity getDetailMaterialProduct() {
        return detailMaterialProduct;
    }

    public void setDetailMaterialProduct(ProductEntity detailMaterialProduct) {
        this.detailMaterialProduct = detailMaterialProduct;
    }


}
