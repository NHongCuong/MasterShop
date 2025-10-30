package com.sportshop.dto;

public class DetailProductColorDTO {

    private Long idColor;
    private Long idProduct;
    private String nameColor;

    public DetailProductColorDTO() {
    }

    public DetailProductColorDTO(Long idColor, Long idProduct, String nameColor) {
        this.idColor = idColor;
        this.idProduct = idProduct;
        this.nameColor = nameColor;
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

    public String getNameColor() {
        return nameColor;
    }

    public void setNameColor(String nameColor) {
        this.nameColor = nameColor;
    }

    @Override
    public String toString() {
        return "DetailProductColorDTO{" +
                "idColor=" + idColor +
                ", idProduct=" + idProduct +
                ", nameColor='" + nameColor + '\'' +
                '}';
    }
}
