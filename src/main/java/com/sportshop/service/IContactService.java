package com.sportshop.service;

import com.sportshop.entity.ContactEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IContactService {
    ContactEntity save(ContactEntity contact);
    java.util.List<ContactEntity> findAll();
    Page<ContactEntity> findAll(Pageable pageable);
    Page<ContactEntity> search(String search, Pageable pageable);
    void delete(Long id);
    ContactEntity updateStatus(Long id, Integer status);
}
