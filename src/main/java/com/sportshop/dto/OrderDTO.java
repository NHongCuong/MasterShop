package com.sportshop.dto;

import com.sportshop.entity.MethodOfPaymentEntity;
import com.sportshop.entity.ShipMethodEntity;


public class OrderDTO {

    public Long id;

    public String CustomerName;

    public String NoteO;

    public String Phone;

    public String AddressO;

    public MethodOfPaymentEntity methodofPayment;

    public ShipMethodEntity shipMethod;

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

    public MethodOfPaymentEntity getMethodofPayment() {
        return methodofPayment;
    }

    public void setMethodofPayment(MethodOfPaymentEntity methodofPayment) {
        this.methodofPayment = methodofPayment;
    }

    public ShipMethodEntity getShipMethod() {
        return shipMethod;
    }

    public void setShipMethod(ShipMethodEntity shipMethod) {
        this.shipMethod = shipMethod;
    }
}
