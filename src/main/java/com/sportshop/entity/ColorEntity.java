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
@Table(name="color")
public class ColorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Color")
    private Long id;

    @Column(name="Name_Color")
    private String nameColor;

    @OneToMany(mappedBy = "detailColor")
    @JsonIgnore
    private List<DetailProductColorEntity> detailproductcolor;
}
