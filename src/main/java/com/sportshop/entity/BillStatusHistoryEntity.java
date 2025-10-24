package com.sportshop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="bill_status_history")
public class BillStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_BSH")
    public Long id;

    // 🔗 Khóa ngoại đến bảng BILL
    @ManyToOne
    @JoinColumn(name = "ID_BILL")
    public BillEntity bill;

    // 🔗 Khóa ngoại đến bảng BILL_STATUS
    @ManyToOne
    @JoinColumn(name = "ID_BS")
    public BillStatusEntity billStatus;

    // 🔗 Khóa ngoại đến bảng USER
    @ManyToOne
    @JoinColumn(name = "ID_User")
    public UserEntity userbillSH;

    @Column(name="Date_BSH")
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
