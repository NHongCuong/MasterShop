package com.sportshop.dto;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.sportshop.entity.CategoryEntity;
import com.sportshop.entity.SupplierEntity;

public class ProductDTO {
	private Long id;
	private String name;
	private String description;
	private Long price;
	private String avatar;
	private String amount;
	private CategoryEntity category;
	private SupplierEntity supplier;

//    public ProductDTO(Long id, String name, String description, Long price, String avatar, String amount, CategoryEntity category, SupplierEntity supplier) {
//        this.id =id;
//        this.name= name;
//        this.description= description;
//        this.price = price;
//        this.avatar = avatar;
//        this.amount = amount;
//        this.category= category;
//        this.supplier = supplier;
//
//    }
//    @Override
//    public String toString() {
//        return "ProductDTO [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price +", avatar=" + avatar +", amount=" + amount +", category=" + category  +", supplier=" + supplier+"]";
//    }

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public CategoryEntity getCategory() {
		return category;
	}
	public void setCategory(CategoryEntity category) {
		this.category = category;
	}
	public SupplierEntity getSupplier() {
		return supplier;
	}
	public void setSupplier(SupplierEntity supplier) {
		this.supplier = supplier;
	}

    public void setPublished(boolean b) {
    }
}
