package com.alaia.pharmX.servicesImpl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaia.pharmX.dtos.ContactDto;
import com.alaia.pharmX.mappers.ContactMapper;
import com.alaia.pharmX.models.Contact;
import com.alaia.pharmX.repositories.ContactRepository;
import com.alaia.pharmX.services.ContactService;

@Service
public class ContactServiceImp implements ContactService{

	@Autowired
    private ContactMapper contactMapper;

	@Autowired
	private ContactRepository contactRepository;

	@Override
	public List<ContactDto> getAllContacts() {
		List<Contact> customers = contactRepository.findAll();
		return customers.stream()
				.map(contactMapper::toDto)
				.toList();
	}
}
