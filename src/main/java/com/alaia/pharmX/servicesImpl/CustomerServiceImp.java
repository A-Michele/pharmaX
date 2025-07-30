package com.alaia.pharmX.servicesImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaia.pharmX.dtos.CustomerDto;
import com.alaia.pharmX.mappers.CustomerMapper;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.services.CustomerService;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerNotFoundException;

@Service
public class CustomerServiceImp implements CustomerService{

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
    private CustomerMapper customerMapper;

	@Override
	public CustomerDto createCustomer(CustomerDto customerDto) {
		if(customerRepository.existsByCF(customerDto.getCF())) {
			throw new CustomerAlreadyExistsException();
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

	@Override
	public CustomerDto updateCustomer(CustomerDto customerDto) {
		Customer existingCustomer = customerRepository.findByCF(customerDto.getCF());
		if(existingCustomer == null ) throw new CustomerNotFoundException("Customer not found with CF : " + customerDto.getCF());

		existingCustomer = customerMapper.toEntity(customerDto);

		Customer updatedCustomer = customerRepository.save(existingCustomer);
		return customerMapper.toDto(updatedCustomer);
	}

	@Override
	public boolean deleteCustomer(long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CustomerDto getCustomerByCF(String CF) {
		Customer customer = customerRepository.findByCF(CF);
		if(customer == null ) throw new CustomerNotFoundException("No Costumer present with CF : " + CF);
		return customerMapper.toDto(customer);
	}
}
