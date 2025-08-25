package com.alaia.pharmX.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.ContactDto;
import com.alaia.pharmX.services.ContactService;

@RestController
@RequestMapping("/contact")
public class ContactController {

	@Autowired
	private ContactService contactService;

	@GetMapping("/all")
	public ResponseEntity<List<ContactDto>> getAllContacts() {
		List<ContactDto> contactDto = contactService.getAllContacts();
		return new ResponseEntity<>(contactDto, HttpStatus.OK);
	}
}
