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

    @Column(name = "created_at")
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    private java.util.Date updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new java.util.Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new java.util.Date();
    }

    @OneToMany(mappedBy = "detailColor")
    @JsonIgnore
    private List<DetailProductColorEntity> detailproductcolor;
}
