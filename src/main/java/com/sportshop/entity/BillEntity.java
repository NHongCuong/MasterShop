package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="bill")
public class BillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Bill")
    private Long id;

    @ManyToOne
    @JoinColumn(name="ID_BS")
    public BillStatusEntity bill;

    @ManyToOne
    @JoinColumn(name="ID_Oder")
    public OderEntity orderbill;

    @OneToMany(mappedBy = "bill")
    @JsonIgnore
    private List<BillStatusHistoryEntity> billstatushistory;

    @Column(name="CreateDate")
    private Date createDate;

    @Column(name="TotalMoney")
    private Long totalMoney;

    @Column(name="VAT_rate")
    private Long vatRate;

    @Column(name="VAT_amount")
    private Long vatAmount;

    @Column(name="TotalMoneyCheckout")
    private float totalMoneyCheckout;

    @Column(name="TotalMoneyaftersaleoff")
    private Long totalMoneyaftersaleoff;


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

    public float getTotalMoneyCheckout() {
        return totalMoneyCheckout;
    }

    public void setTotalMoneyCheckout(float totalMoneyCheckout) {
        this.totalMoneyCheckout = totalMoneyCheckout;
    }

    public Long getTotalMoneyaftersaleoff() {
        return totalMoneyaftersaleoff;
    }

    public void setTotalMoneyaftersaleoff(Long totalMoneyaftersaleoff) {
        this.totalMoneyaftersaleoff = totalMoneyaftersaleoff;
    }

}
