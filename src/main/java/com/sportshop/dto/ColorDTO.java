package com.sportshop.dto;

import java.util.Date;

public class ColorDTO {
    private Long id;
    private String nameColor;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNameColor() { return nameColor; }
    public void setNameColor(String nameColor) { this.nameColor = nameColor; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
