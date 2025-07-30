package com.alaia.pharmX.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
