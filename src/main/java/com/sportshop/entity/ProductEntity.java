package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "productcartdetail")
    @JsonIgnore
    private List<CartDetailEntity> products;

    @OneToMany(mappedBy = "demensions")
    @JsonIgnore
    private List<DimensionsEntity> productDemensions;

    @OneToMany(mappedBy = "detailColorProduct")
    @JsonIgnore
    private List<DetailProductColorEntity> detailproductcolor;

    @OneToMany(mappedBy = "detailMaterialProduct")
    @JsonIgnore
    private List<DetailProductMaterialEntity> detailproductmaterial;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImageEntity> productImages;

    // Added for compatibility with some logic
    public String getName() { return name; }
    public Long getPrice() { return price; }
    public String getAvatar() { return avatar; }

    public void setPublished(boolean b) {
    }
}
