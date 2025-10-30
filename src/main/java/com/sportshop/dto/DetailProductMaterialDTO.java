package com.sportshop.dto;

public class DetailProductMaterialDTO {
    private Long idMaterial;
    private Long idProduct;
    private String nameMaterial;

    public DetailProductMaterialDTO() {
    }

    public DetailProductMaterialDTO(Long idMaterial, Long idProduct, String nameMaterial) {
        this.idMaterial = idMaterial;
        this.idProduct = idProduct;
        this.nameMaterial = nameMaterial;
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
    @Override
    public String toString() {
        return "DetailProductMaterialDTO{" +
                "idMaterial=" + idMaterial +
                ", idProduct=" + idProduct +
                ", nameMaterial='" + nameMaterial + '\'' +
                '}';
    }
}
