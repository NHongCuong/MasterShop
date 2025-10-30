package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

@Entity
@Table(name="detail_product_color")
public class DetailProductColorEntity {

    @EmbeddedId
    private DetailProductColorId id;

    @MapsId("idColor")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_Color")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ColorEntity detailColor;

    @MapsId("idProduct")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="ID_Product")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductEntity detailColorProduct;

    public DetailProductColorId getId() {
        return id;
    }

    public void setId(DetailProductColorId id) {
        this.id = id;
    }

    public ColorEntity getDetailColor() {
        return detailColor;
    }

    public void setDetailColor(ColorEntity detailColor) {
        this.detailColor = detailColor;
    }

    public ProductEntity getDetailColorProduct() {
        return detailColorProduct;
    }

    public void setDetailColorProduct(ProductEntity detailColorProduct) {
        this.detailColorProduct = detailColorProduct;
    }


}
