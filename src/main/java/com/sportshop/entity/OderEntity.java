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
@Table(name = "`order`") // fix reserved keyword
public class OderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Order")
    private Long id;

    @Column(name = "Customer_Name")
    private String customerName;

    @Column(name = "Email_O")
    private String email;

    @Column(name = "Gender_O")
    private String gender;

    @Column(name = "Receiver_Name")
    private String receiverName;

    @Column(name = "Receiver_Phone")
    private String receiverPhone;

    @Column(name="Note_O")
    private String noteO;

    @Column(name="Phone")
    private String phone;

    @Column(name="Address_O")
    private String addressO;

    @ManyToOne
    @JoinColumn(name="ID_MOP")
    private MethodOfPaymentEnity methodofPayment;

    @ManyToOne
    @JoinColumn(name="ID_SM")
    private ShipMethodEntity shipMethod;

    @OneToMany(mappedBy = "orderbill")
    @JsonIgnore
    private List<BillEntity> osderbill;
}
