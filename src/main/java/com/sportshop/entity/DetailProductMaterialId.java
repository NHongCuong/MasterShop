package com.sportshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DetailProductMaterialId implements Serializable {
    @Column(name = "ID_Material")
    private Long idMaterial;

    @Column(name = "ID_Product")
    private Long idProduct;

    public DetailProductMaterialId() {}

    public DetailProductMaterialId(Long idColor, Long idProduct) {
        this.idMaterial = idColor;
        this.idProduct = idProduct;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetailProductMaterialId)) return false;
        DetailProductMaterialId that = (DetailProductMaterialId) o;
        return Objects.equals(idMaterial, that.idMaterial)
                && Objects.equals(idProduct, that.idProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMaterial, idProduct);
    }

    public Long getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Long idMaterial) {
        this.idMaterial = idMaterial;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }
}
