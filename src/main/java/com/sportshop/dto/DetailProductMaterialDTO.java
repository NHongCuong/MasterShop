package com.sportshop.dto;

import java.util.Date;

public class DetailProductMaterialDTO {
    private Long idMaterial;
    private Long idProduct;
    private String nameMaterial;
    private String productName;
    private Date createdAt;
    private Date updatedAt;

    public DetailProductMaterialDTO() {
    }

    public DetailProductMaterialDTO(Long idMaterial, Long idProduct, String nameMaterial, String productName, Date createdAt, Date updatedAt) {
        this.idMaterial = idMaterial;
        this.idProduct = idProduct;
        this.nameMaterial = nameMaterial;
        this.productName = productName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getNameMaterial() {
        return nameMaterial;
    }

    public void setNameMaterial(String nameMaterial) {
        this.nameMaterial = nameMaterial;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DetailProductMaterialDTO{" +
                "idMaterial=" + idMaterial +
                ", idProduct=" + idProduct +
                ", nameMaterial='" + nameMaterial + '\'' +
                ", productName='" + productName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
