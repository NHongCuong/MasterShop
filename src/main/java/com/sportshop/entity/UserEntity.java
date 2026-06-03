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
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_User")
    private Long id;

    @Column(name="Name_User")
    private String nameUser;

    @Column(name="Phone")
    private String phone;

    @Column(name="Email")
    private String email;

    @Column(name="Address")
    private String address;

    @Column
    private String password;

    @Column
    private String verify;

    @Column
    private String regtime;

    @Column
    private String salt;
    
    @ManyToOne
    @JoinColumn(name="ID_UT")
    private UserTypeEntity userType;
    
    @ManyToOne
    @JoinColumn(name="ID_UStatus")
    private UserStatusEntity userStatus;

    @OneToMany(mappedBy="userSC")
    @JsonIgnore
    private List<ShopcartEntity> users;

    @OneToMany(mappedBy = "userbillSH")
    @JsonIgnore
    private List<BillStatusHistoryEntity> userbillstatusHistory;
}
