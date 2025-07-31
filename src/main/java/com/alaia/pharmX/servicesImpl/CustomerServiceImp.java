package com.alaia.pharmX.servicesImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaia.pharmX.dtos.AddressUpdateDto;
import com.alaia.pharmX.dtos.ContactDto;
import com.alaia.pharmX.dtos.ContractUpdateDto;
import com.alaia.pharmX.dtos.CustomerDto;
import com.alaia.pharmX.mappers.ContactMapper;
import com.alaia.pharmX.mappers.CustomerMapper;
import com.alaia.pharmX.models.Contact;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.repositories.ContactRepository;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.services.CustomerService;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerNotFoundException;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class CustomerServiceImp implements CustomerService{

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
    private CustomerMapper customerMapper;

	@Autowired
    private ContactMapper contactMapper;

	@Transactional
	@Override
	public CustomerDto saveCustomer(CustomerDto customerDto) {
		if(customerRepository.existsByCf(customerDto.getCf())) {
			throw new CustomerAlreadyExistsException("Customer already exists with CF : " + customerDto.getCf());
		}

		Customer customer = customerMapper.toEntity(customerDto);
		Customer customerSaved = customerRepository.save(customer);

		return customerMapper.toDto(customerSaved);
	}

	@Override
	public CustomerDto getCustomerById(long id) {
		Customer customer = customerRepository.findById(id).orElseThrow(
        		()-> new CustomerNotFoundException("Customer not found with ID : " + id));
		return customerMapper.toDto(customer);
	}

	@Override
	public List<CustomerDto> getAllCustomers() {
		List<Customer> customers = customerRepository.findAll();
		return customers.stream()
				.map(customerMapper::toDto)
				.toList();
	}

	@Transactional
	@Override
	public CustomerDto updateCustomer(CustomerDto customerDto) {
		Customer existingCustomer = customerRepository.findByCf(customerDto.getCf());
		if(existingCustomer == null ) throw new CustomerNotFoundException("Customer not found with CF : " + customerDto.getCf());

		existingCustomer.setName(customerDto.getName());
		existingCustomer.setShippingAddress(customerDto.getShippingAddress());
		existingCustomer.setBillingAddress(customerDto.getBillingAddress());
		existingCustomer.setCf(customerDto.getCf());

		ContactDto contactDto = customerDto.getContacts();

		if(contactDto != null) {
			Contact contact = existingCustomer.getContacts();

			if(contact == null ) {
				Contact contactCreated = contactMapper.toEntity(contactDto);
				existingCustomer.setContacts(contactCreated);
			} else {
				contact.setEmail(customerDto.getContacts().getEmail());
				contact.setPhoneNumber(customerDto.getContacts().getPhoneNumber());
				existingCustomer.setContacts(contact);
			}
		}
		else {
			existingCustomer.setContacts(null);
		}

		Customer updatedCustomer = customerRepository.save(existingCustomer);
		return customerMapper.toDto(updatedCustomer);
	}

	@Transactional
	@Override
	public CustomerDto deleteCustomer(String cf) {
	    // Trova il cliente per CF
	    Customer customer = customerRepository.findByCf(cf);
	    if (customer == null) {
	        throw new CustomerNotFoundException("Customer not found with CF : " + cf);
	    }
	    customerRepository.delete(customer);
	    return customerMapper.toDto(customer);
	}

	@Override
	public CustomerDto getCustomerByCF(String cf) {
		Customer customer = customerRepository.findByCf(cf);
		if(customer == null ) throw new CustomerNotFoundException("Customer not found with CF : " + cf);
		return customerMapper.toDto(customer);
	}

	@Override
	public CustomerDto getCustomerByParam(Long id, String cf) {
		if(id != null) return this.getCustomerById(id);
		else if(cf != null) return this.getCustomerByCF(cf);
		return null;
	}

	@Override
	public CustomerDto patchContractToCustomerByCf(String cf, ContractUpdateDto customerUpdateDto) {

	    Boolean responseFlagEmail = customerUpdateDto.getDeleteMail();
	    Boolean responseFlagPhoneNumber = customerUpdateDto.getDeletePhoneNumber();
	    String email = customerUpdateDto.getEmail();
	    String phoneNumber = customerUpdateDto.getPhoneNumber();

	    Customer existingCustomer = customerRepository.findByCf(cf);
	    if (existingCustomer == null) {
	        throw new CustomerNotFoundException("Customer not found with CF: " + cf);
	    }

	    Contact existingContact = existingCustomer.getContacts();

	    if (Boolean.TRUE.equals(responseFlagEmail)) {
	        existingContact.setEmail(null);
	    } else if (email != null) {
	        existingContact.setEmail(email);
	    }

	    if (Boolean.TRUE.equals(responseFlagPhoneNumber)) {
	        existingContact.setPhoneNumber(null);
	    } else if (phoneNumber != null) {
	        existingContact.setPhoneNumber(phoneNumber);
	    }

	    Customer updatedCustomer = customerRepository.save(existingCustomer);
	    return customerMapper.toDto(updatedCustomer);
	}

	@Override
	public CustomerDto patchAddressToCustomerByCf(String cf, @Valid AddressUpdateDto addressDto) {

		Boolean responseDeleteShippingAddress = addressDto.getDeleteShippingAddress();
	    Boolean responseDeleteBillingAddress = addressDto.getDeleteBillingAddress();

	    String shippingAddress = addressDto.getShippingAddress();
	    String billingAddress = addressDto.getBillingAddress();

	    Customer existingCustomer = customerRepository.findByCf(cf);
	    if (existingCustomer == null) {
	        throw new CustomerNotFoundException("Customer not found with CF: " + cf);
	    }

	    if (Boolean.TRUE.equals(responseDeleteShippingAddress)) {
	    	existingCustomer.setShippingAddress(null);
	    } else if (shippingAddress != null) {
	    	existingCustomer.setShippingAddress(shippingAddress);
	    }

	    if (Boolean.TRUE.equals(responseDeleteBillingAddress)) {
	    	existingCustomer.setBillingAddress(null);
	    } else if (billingAddress != null) {
	    	existingCustomer.setBillingAddress(billingAddress);
	    }

	    Customer updatedCustomer = customerRepository.save(existingCustomer);
	    return customerMapper.toDto(updatedCustomer);
	}
}
