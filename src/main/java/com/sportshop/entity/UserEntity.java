package com.sportshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
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

    @JsonProperty(access = Access.WRITE_ONLY)
    @Column
    private String password;

    @Column
    private String verify;

    @JsonProperty(access = Access.WRITE_ONLY)
    @Column
    private String salt;

    @Column
    private String avatar;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
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
