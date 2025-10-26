package com.sportshop.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CartDetailId implements Serializable {

    @Column(name = "ID_SC")
    private Long idSC;

    @Column(name = "ID_Product")
    private Long idProduct;

    public CartDetailId() {}

    public CartDetailId(Long idSC, Long idProduct) {
        this.idSC = idSC;
        this.idProduct = idProduct;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartDetailId)) return false;
        CartDetailId that = (CartDetailId) o;
        return Objects.equals(idSC, that.idSC)
                && Objects.equals(idProduct, that.idProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSC, idProduct);
    }

    // Getters / Setters
    public Long getIdSC() { return idSC; }
    public void setIdSC(Long idSC) { this.idSC = idSC; }

    public Long getIdProduct() { return idProduct; }
    public void setIdProduct(Long idProduct) { this.idProduct = idProduct; }
}
