package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity

@Table(name = "product")
public class ProductEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_Product")
	private Long id;
	@Column(name = "Name_Product")
	private String name;
	@Column(name = "Description")
	private String description;
	@Column(name = "Price")
	private Long price;
	@Column(name = "Avatar")
	private String avatar;
	@Column(name = "Amount_Product")
	private String amount;
	@ManyToOne
	@JoinColumn(name = "ID_Category")
	private CategoryEntity category;
	@ManyToOne
	@JoinColumn(name = "ID_S")
	private SupplierEntity supplier;

    @OneToMany(mappedBy = "orderDetail")
    @JsonIgnore
    private List<CartDetailEntity> products;

    @OneToMany(mappedBy = "demensions")
    @JsonIgnore
    private List<DimensionsEntity> productDemensions;

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


//    public void setPublished(boolean b) {
//    }
}
