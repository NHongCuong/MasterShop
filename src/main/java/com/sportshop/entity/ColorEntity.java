package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="color")
public class ColorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Color")
    public Long Id;

    @Column(name="Name_Color")
    public String NameColor;

    @OneToMany(mappedBy = "detailColor")
    @JsonIgnore
    private List<DetailProductColorEntity> detailproductcolor;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getNameColor() {
        return NameColor;
    }

    public void setNameColor(String nameColor) {
        NameColor = nameColor;
    }
    public List<DetailProductColorEntity> getDetailproductcolor() {
        return detailproductcolor;
    }

    public void setDetailproductcolor(List<DetailProductColorEntity> detailproductcolor) {
        this.detailproductcolor = detailproductcolor;
    }

}
