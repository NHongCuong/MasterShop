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
