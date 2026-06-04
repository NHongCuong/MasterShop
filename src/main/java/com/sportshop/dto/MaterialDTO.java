package com.sportshop.dto;

import java.util.Date;

public class MaterialDTO {
    private Long id;
    private String nameMaterial;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNameMaterial() { return nameMaterial; }
    public void setNameMaterial(String nameMaterial) { this.nameMaterial = nameMaterial; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
