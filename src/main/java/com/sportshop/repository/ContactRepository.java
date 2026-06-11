package com.sportshop.repository;

import com.sportshop.entity.ContactEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Long> {
    @Query("SELECT c FROM ContactEntity c WHERE " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.subject) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ContactEntity> searchContacts(@Param("search") String search, Pageable pageable);
}
