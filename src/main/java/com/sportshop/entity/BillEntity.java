package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="bill")
public class BillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_Bill")
    private Long id;

    @ManyToOne
    @JoinColumn(name="ID_BS")
    private BillStatusEntity bill;

    @ManyToOne
    @JoinColumn(name="ID_Oder")
    private OderEntity orderbill;

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
    private Float totalMoneyCheckout;

    @Column(name="TotalMoneyaftersaleoff")
    private Long totalMoneyaftersaleoff;

    @Column(name="discount")
    private Long discount;
}
