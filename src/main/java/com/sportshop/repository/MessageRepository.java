package com.sportshop.repository;

import com.sportshop.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    
    @Query("SELECT m FROM MessageEntity m WHERE " +
           "(m.fromUser = :user1 AND m.toUser = :user2) OR " +
           "(m.fromUser = :user2 AND m.toUser = :user1) " +
           "ORDER BY m.createdAt ASC")
    List<MessageEntity> findChatHistory(@Param("user1") String user1, @Param("user2") String user2);
}
