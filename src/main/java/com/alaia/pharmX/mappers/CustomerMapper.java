package com.alaia.pharmX.mappers;

import com.alaia.pharmX.dtos.CustomerDto;
import com.alaia.pharmX.models.Contact;
import com.alaia.pharmX.models.Customer;

public class CustomerMapper {

    public CustomerDto toDto(Customer customer) {
        if (customer == null) return null;

        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setShippingAddress(customer.getShippingAddress());
        dto.setBillingAddress(customer.getBillingAddress());
        dto.setCF(customer.getCF());

        if (customer.getContacts() != null) {
        	dto.setContacts(ContactMapper.toDto(customer.getContacts()));
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
        customer.setCF(dto.getCF());

        if (dto.getContacts() != null) {
            Contact contact = ContactMapper.toEntity(dto.getContacts());

            contact.setCustomer(customer);
            customer.setContacts(contact);
        }

        return customer;
    }
}