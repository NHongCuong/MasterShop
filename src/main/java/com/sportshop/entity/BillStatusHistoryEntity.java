package com.sportshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="bill_status_history")
public class BillStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID_BSH")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_BILL")
    private BillEntity bill;

    @ManyToOne
    @JoinColumn(name = "ID_BS")
    private BillStatusEntity billStatus;

    @ManyToOne
    @JoinColumn(name = "ID_User")
    private UserEntity userbillSH;

    @Column(name="Date_BSH")
    private LocalDateTime dateBSH;
}
