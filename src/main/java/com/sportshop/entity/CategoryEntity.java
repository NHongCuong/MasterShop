package com.sportshop.entity;

import java.util.Date;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "category")
public class CategoryEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_Category")
    public Long id;
	@Column(name = "Name_Category")
    public String name;
	@Column(name = "Icon")
    public String icon;

	@Column(name = "created_at")
	public Date createdAt;

	@Column(name = "updated_at")
	public Date updatedAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = new Date();
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = new Date();
	}

	@OneToMany(mappedBy="category")
	@JsonIgnore
	private List<ProductEntity> products;

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
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	public List<ProductEntity> getProducts() {
		return products;
	}
	public void setProducts(List<ProductEntity> products) {
		this.products = products;
	}
}
