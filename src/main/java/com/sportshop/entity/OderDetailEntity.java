package com.sportshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="order_detail")
public class OderDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Order") // Đây là Khóa chính (PK) tự tăng 1,2,3... kệ nó.
    private Long id;

    @ManyToOne
    @JoinColumn(name="id_order_header_fk") // Đây là Khóa ngoại (FK) liên kết tới Đơn hàng.
    private OderEntity order;

    @ManyToOne
    @JoinColumn(name="ID_Product") // Liên kết với bảng Product
    private ProductEntity product;

    @Column(name="Amount")
    private Long amount;

    @Column(name="Price")
    private Long price;

    @ManyToOne
    @JoinColumn(name="ID_Color")
    private ColorEntity color;

    @ManyToOne
    @JoinColumn(name="ID_Material")
    private MaterialEntity material;

    @ManyToOne
    @JoinColumn(name="ID_D")
    private DimensionsEntity dimensions;
}
