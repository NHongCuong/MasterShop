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
    @Column(name="ID_Order")
    private Long id;

    @ManyToOne
    @JoinColumn(name="ID_SC")
    private ShopcartEntity shopcartOD;
}
