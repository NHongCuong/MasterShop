package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="methodofpayment")
public class MethodOfPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MOP")
    public Long id;

    @Column(name= "Name_MOP")
    public String name_mop;

    @OneToMany(mappedBy = "methodofPayment")
    @JsonIgnore
    private List<OrderEntity> oders;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName_mop() {
        return name_mop;
    }

    public void setName_mop(String name_mop) {
        this.name_mop = name_mop;
    }

    public List<OrderEntity> getOders() {
        return oders;
    }

    public void setOders(List<OrderEntity> oders) {
        this.oders = oders;
    }
}
