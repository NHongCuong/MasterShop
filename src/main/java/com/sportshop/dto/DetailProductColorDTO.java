package com.sportshop.dto;

public class DetailProductColorDTO {

    private Long idColor;
    private Long idProduct;
    private String nameColor;
    private String productName;
    private java.util.Date createdAt;
    private java.util.Date updatedAt;

    public DetailProductColorDTO() {
    }

    public DetailProductColorDTO(Long idColor, Long idProduct, String nameColor, String productName, java.util.Date createdAt, java.util.Date updatedAt) {
        this.idColor = idColor;
        this.idProduct = idProduct;
        this.nameColor = nameColor;
        this.productName = productName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public java.util.Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }

    public java.util.Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.util.Date updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "DetailProductColorDTO{" +
                "idColor=" + idColor +
                ", idProduct=" + idProduct +
                ", nameColor='" + nameColor + '\'' +
                '}';
    }
}
