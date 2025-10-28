package com.sportshop.entity;

import javax.persistence.*;

@Entity
@Table(name="color")
public class ColorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Color")
    public Long Id;

    @Column(name="Name_Color")
    public String NameColor;

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

}
