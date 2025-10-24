package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity

@Table(name = "order")
public class OderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Order")
    public Long id;

    @Column(name = "Customer_Name")
    public String CustomerName;

    @Column(name="Note_O")
    public String NoteO;

    @Column(name="Phone")
    public String Phone;

    @Column(name="Address_O")
    public String AddressO;

    @ManyToOne
    @JoinColumn(name="ID_MOP")
    public MethodOfPaymentEnity methodofPayment;

    @ManyToOne
    @JoinColumn(name="ID_SM")
    public ShipMethodEntity shipMethod;

    @OneToMany(mappedBy = "orderbill")
    @JsonIgnore
    private List<BillEntity> osderbill;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getNoteO() {
        return NoteO;
    }

    public void setNoteO(String noteO) {
        NoteO = noteO;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getAddressO() {
        return AddressO;
    }

    public void setAddressO(String addressO) {
        AddressO = addressO;
    }

    public MethodOfPaymentEnity getMethodofPayment() {
        return methodofPayment;
    }

    public void setMethodofPayment(MethodOfPaymentEnity methodofPayment) {
        this.methodofPayment = methodofPayment;
    }

    public ShipMethodEntity getShipMethod() {
        return shipMethod;
    }

    public void setShipMethod(ShipMethodEntity shipMethod) {
        this.shipMethod = shipMethod;
    }


}
