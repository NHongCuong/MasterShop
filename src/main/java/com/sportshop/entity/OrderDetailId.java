package com.sportshop.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderDetailId implements Serializable {
    @Column(name = "ID_Order")
    private Long idOrder;

    @Column(name = "ID_SC")
    private Long idSC;

    public OrderDetailId(Long idOrder, Long idSC) {
        this.idOrder = idOrder;
        this.idSC = idSC;
    }
    public OrderDetailId() {}

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderDetailId)) return false;
        OrderDetailId that = (OrderDetailId) o;
        return Objects.equals(idOrder, that.idOrder)
                && Objects.equals(idSC, that.idSC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOrder, idSC);
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public Long getIdSC() {
        return idSC;
    }

    public void setIdSC(Long idSC) {
        this.idSC = idSC;
    }


}
