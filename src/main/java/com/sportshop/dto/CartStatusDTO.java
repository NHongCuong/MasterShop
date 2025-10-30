package com.sportshop.dto;

import java.util.Date;

public class CartStatusDTO {
    public Long id;
    public String nameCS;
    public Date created_at;
    public Date updated_at;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameCS() {
        return nameCS;
    }

    public void setNameCS(String nameCS) {
        this.nameCS = nameCS;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
