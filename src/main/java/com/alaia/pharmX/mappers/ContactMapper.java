package com.alaia.pharmX.mappers;

import org.springframework.stereotype.Component;

import com.alaia.pharmX.dtos.ContactDto;
import com.alaia.pharmX.models.Contact;

@Component
public class ContactMapper {

    public ContactDto toDto(Contact contact) {
        if (contact == null) return null;

        ContactDto dto = new ContactDto();
        dto.setId(contact.getId());
        dto.setEmail(contact.getEmail());
        dto.setPhoneNumber(contact.getPhoneNumber());

        //Non settiamo customer per evitare ricorsione
        return dto;
    }

    public Contact toEntity(ContactDto dto) {
        if (dto == null) return null;

        Contact contact = new Contact();
        contact.setId(dto.getId());
        contact.setEmail(dto.getEmail());
        contact.setPhoneNumber(dto.getPhoneNumber());

        //Anche qui, customer verrà impostato altrove, se necessario
        return contact;
    }
}