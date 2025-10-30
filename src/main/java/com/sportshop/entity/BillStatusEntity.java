package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="bill_status")
public class BillStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_BS")
    public Long id;

    @Column(name="Name_BS")
    public String nameBS;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @OneToMany(mappedBy = "bill")
    @JsonIgnore
    private List<BillEntity> billstatus;

    @OneToMany(mappedBy = "billStatus")
    @JsonIgnore
    private List<BillStatusHistoryEntity> billstatusHistory;

    public String getNameBS() {
        return nameBS;
    }

    public void setNameBS(String nameBS) {
        this.nameBS = nameBS;
    }

    public List<BillEntity> getBillstatus() {
        return billstatus;
    }

    public void setBillstatus(List<BillEntity> billstatus) {
        this.billstatus = billstatus;
    }

    public List<BillStatusHistoryEntity> getBillstatusHistory() {
        return billstatusHistory;
    }

    public void setBillstatusHistory(List<BillStatusHistoryEntity> billstatusHistory) {
        this.billstatusHistory = billstatusHistory;
    }

}
