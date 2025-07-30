package com.alaia.pharmX.services;

import java.util.List;

import com.alaia.pharmX.dtos.CustomerDto;

public interface CustomerService {
	CustomerDto createCustomer(CustomerDto customerDto);
	CustomerDto getCustomerById(long id);
	List<CustomerDto> getAllCustomers();
	CustomerDto updateCustomer(CustomerDto customerDto);
	boolean deleteCustomer(long id);
	CustomerDto getCustomerByCF(String CF);
}
