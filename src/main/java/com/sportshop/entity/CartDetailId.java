package com.sportshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class CartDetailId implements Serializable {

    @Column(name = "ID_SC")
    private Long idSC;

    @Column(name = "ID_Product")
    private Long idProduct;
}
