package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

	@Column(name = "Sold_Quantity")
	private Long soldQuantity;

	@Column(name = "Discount_Percent")
	private Integer discountPercent;

	@Column(name = "created_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "updated_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = new Date();
		}
	}

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
    @JsonIgnoreProperties("demensions")
    @BatchSize(size = 20)
    private Set<DimensionsEntity> productDemensions;

    @OneToMany(mappedBy = "detailColorProduct")
    @JsonIgnoreProperties("detailColorProduct")
    @BatchSize(size = 20)
    private Set<DetailProductColorEntity> detailproductcolor;

    @OneToMany(mappedBy = "detailMaterialProduct")
    @JsonIgnoreProperties("detailMaterialProduct")
    @BatchSize(size = 20)
    private Set<DetailProductMaterialEntity> detailproductmaterial;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImageEntity> productImages;

    @ManyToOne
    @JoinColumn(name = "ID_Voucher")
    private VoucherEntity voucher;

    // Added for compatibility with some logic
    public String getName() { return name; }
    public Long getPrice() { return price; }
    public String getAvatar() { return avatar; }

    public void setPublished(boolean b) {
    }
}
