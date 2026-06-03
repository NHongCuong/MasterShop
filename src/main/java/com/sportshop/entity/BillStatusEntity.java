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
@Table(name="bill_status")
public class BillStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_BS")
    private Long id;

    @Column(name="Name_BS")
    private String nameBS;

    @OneToMany(mappedBy = "bill")
    @JsonIgnore
    private List<BillEntity> billstatus;

    @OneToMany(mappedBy = "billStatus")
    @JsonIgnore
    private List<BillStatusHistoryEntity> billstatusHistory;
}
