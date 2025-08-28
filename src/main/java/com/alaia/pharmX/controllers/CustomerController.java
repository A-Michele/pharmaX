package com.alaia.pharmX.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.AddressUpdateDto;
import com.alaia.pharmX.dtos.ContractUpdateDto;
import com.alaia.pharmX.dtos.CustomerDto;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.services.CustomerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@GetMapping("/all")
	public ResponseEntity<List<CustomerDto>> getAllCustomers() {
		List<CustomerDto> customerDto = customerService.getAllCustomers();
		return new ResponseEntity<>(customerDto, HttpStatus.OK);
	}

	@GetMapping("/customer_id/{id}")
	public ResponseEntity<CustomerDto> getCustomerById(@PathVariable int id) {
		CustomerDto customerDto =  customerService.getCustomerById(id);
		return new ResponseEntity<>(customerDto, HttpStatus.OK);
	}

	@GetMapping("/customer_cf/{cf}")
	public ResponseEntity<CustomerDto> getCustomerByCF(@PathVariable String cf) {
		CustomerDto customerDto =  customerService.getCustomerByCF(cf);
		return new ResponseEntity<>(customerDto, HttpStatus.OK);
	}

	@GetMapping()
	public ResponseEntity<CustomerDto> getCustomerByParams(
			@RequestParam(name = "id", required = false) Long id,
			@RequestParam(name = "cf", required = false) String cf) {
		CustomerDto customerDto = customerService.getCustomerByParam(id, cf);
		return new ResponseEntity<>(customerDto, HttpStatus.CREATED);
	}

	@PostMapping("/add")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
		CustomerDto createdCustomerDto = customerService.saveCustomer(customerDto);
    	return new ResponseEntity<>(createdCustomerDto, HttpStatus.CREATED);
    }

	@PutMapping("/update")
    public ResponseEntity<CustomerDto> updateUser(@Valid @RequestBody CustomerDto customerDto) {
		CustomerDto updatedCustomerDto = customerService.updateCustomer(customerDto);
    	return new ResponseEntity<>(updatedCustomerDto, HttpStatus.OK);
    }

	@PatchMapping("/updateContract")
    public ResponseEntity<CustomerDto> patchContractToCustomer(@RequestParam(name = "cf") String cf, @Valid @RequestBody ContractUpdateDto contractUpdateDto) {
		CustomerDto customerUpdate = customerService.patchContractToCustomerByCf(cf, contractUpdateDto);
        return new ResponseEntity<>(customerUpdate, HttpStatus.OK);
    }

	@PatchMapping("/updateAddress")
    public ResponseEntity<CustomerDto> patchAddressToCustomer(@RequestParam(name = "cf") String cf, @Valid @RequestBody AddressUpdateDto addressDto) {
		CustomerDto customerUpdate = customerService.patchAddressToCustomerByCf(cf, addressDto);
        return new ResponseEntity<>(customerUpdate, HttpStatus.OK);
    }

	@DeleteMapping("/delete")
    public ResponseEntity<CustomerDto> deleteCustomerDto(@RequestParam(name = "cf") String cf) {
		CustomerDto customerDto = customerService.deleteCustomer(cf);
        return new ResponseEntity<>(customerDto, HttpStatus.OK);
    }

	@GetMapping("/customer_email/{email}")
	public ResponseEntity<CustomerDto> getCustomerByEmail(@PathVariable String email) {
		CustomerDto customerDto =  customerService.getCustomerByEmail(email);
		return new ResponseEntity<>(customerDto, HttpStatus.OK);
	}

	@GetMapping("/orders")
	public ResponseEntity<List<OrderDto>> getOrdersCustomerByEmail(@RequestParam(name = "customer_cf") String customer_cf) {
		List<OrderDto> orders =  customerService.getOrdersByCF(customer_cf);
		return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	@GetMapping("/ordersByType")
	public ResponseEntity<List<OrderDto>> getOrdersCustomerByCfAndType(@RequestParam(name = "customer_cf") String customer_cf, @RequestParam(name = "type") String type) {
		List<OrderDto> orders =  customerService.getOrdersByCFAndType(customer_cf, type);
		return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	@DeleteMapping("/delete-safely")
    public ResponseEntity<CustomerDto> deleteSafelyCustomerDto(@RequestParam(name = "cf") String cf) {
		CustomerDto customerDto = customerService.deleteCustomerSafely(cf);
        return new ResponseEntity<>(customerDto, HttpStatus.OK);
    }
}