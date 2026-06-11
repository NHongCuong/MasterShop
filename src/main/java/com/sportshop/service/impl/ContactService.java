package com.sportshop.service.impl;

import com.sportshop.entity.ContactEntity;
import com.sportshop.repository.ContactRepository;
import com.sportshop.service.IContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ContactService implements IContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Override
    public java.util.List<ContactEntity> findAll() {
        return contactRepository.findAll();
    }

    @Override
    public ContactEntity save(ContactEntity contact) {
        return contactRepository.save(contact);
    }

    @Override
    public Page<ContactEntity> findAll(Pageable pageable) {
        return contactRepository.findAll(pageable);
    }

    @Override
    public Page<ContactEntity> search(String search, Pageable pageable) {
        return contactRepository.searchContacts(search, pageable);
    }

    @Override
    public void delete(Long id) {
        contactRepository.deleteById(id);
    }

    @Override
    public ContactEntity updateStatus(Long id, Integer status) {
        ContactEntity contact = contactRepository.findById(id).orElseThrow(() -> new RuntimeException("Contact not found"));
        contact.setStatus(status);
        return contactRepository.save(contact);
    }
}
