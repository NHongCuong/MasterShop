package com.sportshop.dto;

import java.util.Date;

public class DimensionsDTO {
    private Long id;
    private String nameD;
    private Date createdAt;
    private Date updatedAt;
    private Long idProduct;

    public DimensionsDTO() {
    }

    public DimensionsDTO(Long id, String nameD, Date createdAt, Date updatedAt, Long idProduct) {
        this.id = id;
        this.nameD = nameD;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.idProduct = idProduct;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameD() {
        return nameD;
    }

    public void setNameD(String nameD) {
        this.nameD = nameD;
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

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }
}
