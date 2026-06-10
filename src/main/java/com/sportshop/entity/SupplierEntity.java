package com.sportshop.entity;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "supplier")
public class SupplierEntity {
	@Id
	@Column(name ="ID_S")
	private Long id;
	@Column(name = "Name_S")
	private String name;
	@Column(name = "Address_S")
	private String address;
	@Column(name = "Phone_S")
	private String phone;
	@Column(name = "Email_S")
	private String email;
	@Column(name = "Website_S")
	private String website;

	@OneToMany(mappedBy="supplier")
	@JsonIgnore
	private List<ProductEntity> products;

	@Column(name = "Created_At")
	@jakarta.persistence.Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
	private java.util.Date createdAt;

	@Column(name = "Updated_At")
	@jakarta.persistence.Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
	private java.util.Date updatedAt;

	@jakarta.persistence.PrePersist
	protected void onCreate() {
		createdAt = new java.util.Date();
	}

	@jakarta.persistence.PreUpdate
	protected void onUpdate() {
		updatedAt = new java.util.Date();
	}

	public java.util.Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(java.util.Date createdAt) {
		this.createdAt = createdAt;
	}

	public java.util.Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(java.util.Date updatedAt) {
		this.updatedAt = updatedAt;
	}

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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public List<ProductEntity> getProducts() {
		return products;
	}

	public void setProducts(List<ProductEntity> products) {
		this.products = products;
	}




}
