package com.sportshop.dto;

import com.sportshop.entity.BillStatusEntity;
import com.sportshop.entity.OderEntity;
import java.util.Date;

public class BillDTO {
    public Long id;
    public BillStatusEntity bill;
    public OderEntity orderbill;
    public Date createDate;
    public Long totalMoney;
    public Long vatRate;
    public Long vatAmount;
    public Float totalMoneyCheckout;
    public Long totalMoneyaftersaleoff;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BillStatusEntity getBill() {
        return bill;
    }

    public void setBill(BillStatusEntity bill) {
        this.bill = bill;
    }

    public OderEntity getOrderbill() {
        return orderbill;
    }

    public void setOrderbill(OderEntity orderbill) {
        this.orderbill = orderbill;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(Long totalMoney) {
        this.totalMoney = totalMoney;
    }

    public Long getVatRate() {
        return vatRate;
    }

    public void setVatRate(Long vatRate) {
        this.vatRate = vatRate;
    }

    public Long getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(Long vatAmount) {
        this.vatAmount = vatAmount;
    }

    public Float getTotalMoneyCheckout() {
        return totalMoneyCheckout;
    }

    public void setTotalMoneyCheckout(Float totalMoneyCheckout) {
        this.totalMoneyCheckout = totalMoneyCheckout;
    }

    public Long getTotalMoneyaftersaleoff() {
        return totalMoneyaftersaleoff;
    }

    public void setTotalMoneyaftersaleoff(Long totalMoneyaftersaleoff) {
        this.totalMoneyaftersaleoff = totalMoneyaftersaleoff;
    }

}
