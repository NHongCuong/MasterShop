package com.sportshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "contact")
public class ContactEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Contact")
    private Long id;

    @Column(name = "Full_Name")
    private String fullName;

    @Column(name = "Email")
    private String email;

    @Column(name = "Phone_Number")
    private String phoneNumber;

    @Column(name = "Subject")
    private String subject;

    @Column(name = "Message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "Status")
    private Integer status; // 0: New, 1: Processed

    @Column(name = "Created_At")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        if (status == null) {
            status = 0;
        }
    }
}
