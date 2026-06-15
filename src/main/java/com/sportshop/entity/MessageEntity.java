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
@Table(name = "messages")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="from_user")
    private String fromUser;

    @Column(name="to_user")
    private String toUser;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "reply_to_id")
    private Long replyToId;

    @Column(name = "reactions", columnDefinition = "TEXT")
    private String reactions;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
}
