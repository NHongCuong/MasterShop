package com.sportshop.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DetailProductColorId implements Serializable {

    @Column(name = "ID_Color")
    private Long idColor;

    @Column(name = "ID_Product")
    private Long idProduct;

    public DetailProductColorId() {}

    public DetailProductColorId(Long idColor, Long idProduct) {
        this.idColor = idColor;
        this.idProduct = idProduct;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetailProductColorId)) return false;
        DetailProductColorId that = (DetailProductColorId) o;
        return Objects.equals(idColor, that.idColor)
                && Objects.equals(idProduct, that.idProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idColor, idProduct);
    }

    public Long getIdColor() {
        return idColor;
    }

    public void setIdColor(Long idColor) {
        this.idColor = idColor;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

}
