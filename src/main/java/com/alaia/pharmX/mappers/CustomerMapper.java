package com.alaia.pharmX.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.CustomerDto;
import com.alaia.pharmX.models.Contact;
import com.alaia.pharmX.models.Customer;

@Component
public class CustomerMapper {

	@Autowired
    private ContactMapper contactMapper;

    public CustomerDto toDto(Customer customer) {
        if (customer == null) return null;

        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setShippingAddress(customer.getShippingAddress());
        dto.setBillingAddress(customer.getBillingAddress());
        dto.setCf(customer.getCf());

        if (customer.getContacts() != null) {
        	dto.setContacts(contactMapper.toDto(customer.getContacts()));
        }

        return dto;
    }

    public Customer toEntity(CustomerDto dto) {
        if (dto == null) return null;

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setShippingAddress(dto.getShippingAddress());
        customer.setBillingAddress(dto.getBillingAddress());
        customer.setCf(dto.getCf());

        if (dto.getContacts() != null) {
            Contact contact = contactMapper.toEntity(dto.getContacts());
            customer.setContacts(contact);
        }

        return customer;
    }
}