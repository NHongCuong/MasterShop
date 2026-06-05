package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity

@Table(name = "shoppingcart")
public class ShopcartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SC")
    public Long id;


    @ManyToOne
    @JoinColumn(name="ID_User")
    public UserEntity userSC;


    @ManyToOne
    @JoinColumn(name="ID_CS")
    public  CartStatusEntity cartStatus;

    @OneToMany(mappedBy = "shopcartdetail")
    @JsonIgnore
    private List<CartDetailEntity> cartDetail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUserSC() {
        return userSC;
    }

    public void setUserSC(UserEntity userSC) {
        this.userSC = userSC;
    }

    public CartStatusEntity getCartStatus() {
        return cartStatus;
    }

    public void setCartStatus(CartStatusEntity cartStatus) {
        this.cartStatus = cartStatus;
    }


}
