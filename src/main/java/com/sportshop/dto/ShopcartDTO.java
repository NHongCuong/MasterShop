package com.sportshop.dto;

import com.sportshop.entity.CartStatusEntity;
import com.sportshop.entity.UserEntity;

public class ShopcartDTO {
    public Long id;
    public UserEntity userSC;
    public CartStatusEntity cartStatus;

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
