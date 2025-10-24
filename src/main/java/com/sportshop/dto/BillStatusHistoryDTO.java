package com.sportshop.dto;

import com.sportshop.entity.BillEntity;
import com.sportshop.entity.BillStatusEntity;
import com.sportshop.entity.UserEntity;

import java.time.LocalDateTime;

public class BillStatusHistoryDTO {
    public Long id;
    public BillEntity bill;
    public BillStatusEntity billStatus;
    public UserEntity userbillSH;
    public LocalDateTime Date_BSH;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BillEntity getBill() {
        return bill;
    }

    public void setBill(BillEntity bill) {
        this.bill = bill;
    }

    public BillStatusEntity getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(BillStatusEntity billStatus) {
        this.billStatus = billStatus;
    }

    public UserEntity getUserbillSH() {
        return userbillSH;
    }

    public void setUserbillSH(UserEntity userbillSH) {
        this.userbillSH = userbillSH;
    }

    public LocalDateTime getDate_BSH() {
        return Date_BSH;
    }

    public void setDate_BSH(LocalDateTime date_BSH) {
        Date_BSH = date_BSH;
    }


}
