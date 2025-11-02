package com.sportshop.dto;

public class OrderDetailDTO {
    private Long idOrder;

    private Long idSC;


    public OrderDetailDTO(Long idOrder, Long idSC) {
        this.idOrder = idOrder;
        this.idSC = idSC;
    }

    public OrderDetailDTO() {}

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


    @Override
    public String toString() {
        return "OrderDetailDTO{" +
                "idOrder=" + idOrder +
                ", idSC=" + idSC +
                '}';
    }


}
