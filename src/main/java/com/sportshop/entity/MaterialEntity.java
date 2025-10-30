package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="material")
public class MaterialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Material")
    public Long Id;

    @Column(name="Name_Material")
    public String NameMaterial;

    @OneToMany(mappedBy = "materialcartdetail")
    @JsonIgnore
    private List<CartDetailEntity> material;

    @OneToMany(mappedBy = "detailMaterial")
    @JsonIgnore
    private List<DetailProductMaterialEntity> detailproductmaterial;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getNameMaterial() {
        return NameMaterial;
    }

    public void setNameMaterial(String nameMaterial) {
        NameMaterial = nameMaterial;
    }

    public List<CartDetailEntity> getMaterial() {
        return material;
    }

    public void setMaterial(List<CartDetailEntity> material) {
        this.material = material;
    }


    public List<DetailProductMaterialEntity> getDetailproductmaterial() {
        return detailproductmaterial;
    }

    public void setDetailproductmaterial(List<DetailProductMaterialEntity> detailproductmaterial) {
        this.detailproductmaterial = detailproductmaterial;
    }


}
