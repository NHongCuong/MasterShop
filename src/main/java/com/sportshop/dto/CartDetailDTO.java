package com.sportshop.dto;

import java.util.Date;

public class CartDetailDTO {

    private Long idCartDetail;
    private Long idSC;
    private Long idProduct;
    private Long idMaterial;
    private Long idD;

    private Long amountCD;
    private Long idColor;
    private Date createdAt;
    private Date updatedAt;

    // Thông tin mở rộng hiển thị cho frontend
    private String productName;
    private Long productPrice;
    private String productAvatar;
    private String materialName;
    private String dimensionName;
    private String colorName; // Thêm tên màu
    private String voucherCode; // Mã voucher áp dụng cho sản phẩm này

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public Long getProductPrice() { return productPrice; }
    public void setProductPrice(Long productPrice) { this.productPrice = productPrice; }

    public String getProductAvatar() { return productAvatar; }
    public void setProductAvatar(String productAvatar) { this.productAvatar = productAvatar; }

    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }

    // Getters & Setters
    public Long getIdCartDetail() { return idCartDetail; }
    public void setIdCartDetail(Long idCartDetail) { this.idCartDetail = idCartDetail; }

    public Long getIdSC() { return idSC; }
    public void setIdSC(Long idSC) { this.idSC = idSC; }

    public Long getIdProduct() { return idProduct; }
    public void setIdProduct(Long idProduct) { this.idProduct = idProduct; }

    public Long getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Long idMaterial) { this.idMaterial = idMaterial; }

    public Long getIdD() { return idD; }
    public void setIdD(Long idD) { this.idD = idD; }

    public Long getAmountCD() { return amountCD; }
    public void setAmountCD(Long amountCD) { this.amountCD = amountCD; }

    public Long getIdColor() { return idColor; }
    public void setIdColor(Long idColor) { this.idColor = idColor; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getDimensionName() { return dimensionName; }
    public void setDimensionName(String dimensionName) { this.dimensionName = dimensionName; }
}
