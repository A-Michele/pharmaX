package com.alaia.pharmX.services;

import java.util.List;

import com.alaia.pharmX.dtos.AddressUpdateDto;
import com.alaia.pharmX.dtos.ContractUpdateDto;
import com.alaia.pharmX.dtos.CustomerDto;
import com.alaia.pharmX.dtos.OrderDto;

public interface CustomerService {
	CustomerDto saveCustomer(CustomerDto customerDto);
	CustomerDto getCustomerById(long id);
	List<CustomerDto> getAllCustomers();
	CustomerDto updateCustomer(CustomerDto customerDto);
	CustomerDto deleteCustomer(String cf);
	CustomerDto getCustomerByCF(String cf);
	CustomerDto getCustomerByParam(Long id, String cf);
	CustomerDto patchContractToCustomerByCf(String cf, ContractUpdateDto customerDto);
	CustomerDto patchAddressToCustomerByCf(String cf, AddressUpdateDto addressDto);
	CustomerDto getCustomerByEmail(String email);
	List<OrderDto> getOrdersByCF(String cf);
	List<OrderDto> getOrdersByCFAndType(String cf, String state);
	CustomerDto deleteCustomerSafely(String cf);
}
