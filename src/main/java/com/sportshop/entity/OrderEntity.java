package com.sportshop.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "`order`")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Order")
    private Long id;

    @Column(name = "Customer_Name")
    private String customerName;

    @Column(name="Note_O")
    private String noteO;

    @Column(name="Phone_O")
    private String phone;

    @Column(name="Address_O")
    private String addressO;

    @ManyToOne
    @JoinColumn(name="ID_MOP")
    private MethodOfPaymentEntity methodofPayment;

    @ManyToOne
    @JoinColumn(name="ID_SM")
    private ShipMethodEntity shipMethod;

    @OneToMany(mappedBy = "orderbill")
    @JsonIgnore
    private List<BillEntity> osderbill;

    @OneToMany(mappedBy = "orderEntity")
    @JsonIgnore
    private List<OrderDetailEntity> orderDetailEntities;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getNoteO() { return noteO; }
    public void setNoteO(String noteO) { this.noteO = noteO; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddressO() { return addressO; }
    public void setAddressO(String addressO) { this.addressO = addressO; }

    public MethodOfPaymentEntity getMethodofPayment() { return methodofPayment; }
    public void setMethodofPayment(MethodOfPaymentEntity methodofPayment) { this.methodofPayment = methodofPayment; }

    public ShipMethodEntity getShipMethod() { return shipMethod; }
    public void setShipMethod(ShipMethodEntity shipMethod) { this.shipMethod = shipMethod; }

    public List<BillEntity> getOsderbill() { return osderbill; }
    public void setOsderbill(List<BillEntity> osderbill) { this.osderbill = osderbill; }

    public List<OrderDetailEntity> getOrderDetailEntities() { return orderDetailEntities; }
    public void setOrderDetailEntities(List<OrderDetailEntity> orderDetailEntities) { this.orderDetailEntities = orderDetailEntities; }
}
