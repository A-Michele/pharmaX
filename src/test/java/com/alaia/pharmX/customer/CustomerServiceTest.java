package com.alaia.pharmX.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.AddressUpdateDto;
import com.alaia.pharmX.dtos.ContactDto;
import com.alaia.pharmX.dtos.ContractUpdateDto;
import com.alaia.pharmX.dtos.CustomerDto;
import com.alaia.pharmX.mappers.ContactMapper;
import com.alaia.pharmX.mappers.CustomerMapper;
import com.alaia.pharmX.models.Contact;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.servicesImpl.CustomerServiceImp;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private CustomerMapper customerMapper;

	@Mock
	private ContactMapper contactMapper;

	@InjectMocks
	private CustomerServiceImp customerService;

	private CustomerDto customerDto;
	private Customer customer;
	private ContactDto contactDto;

	private ContractUpdateDto contractUpdateDto;
	private AddressUpdateDto addressUpdateDto;

	@BeforeEach
	void setUp() {
	    contactDto = new ContactDto(1L, "john@example.com", "1234567890");
	    customerDto = new CustomerDto(1L, "John Doe", "123 Street", "456 Avenue", contactDto, "1234567890123456");

	    // Non impostare manualmente l'id per Contact (viene generato automaticamente dal DB)
	    customer = new Customer(1L, "John Doe", "123 Street", "456 Avenue", new Contact(0, "john@example.com", "1234567890"), "1234567890123456");

	    contractUpdateDto = new ContractUpdateDto(1L, "newemail@example.com", "9876543210", false, false);
	    addressUpdateDto = new AddressUpdateDto("789 Road", "1010 Boulevard", false, false);
	}

	//----------->TEST SAVE CUSTOMER<-----------

	@Test
	void saveCustomer_ShouldReturnCustomerDto_WhenCustomerDoesNotExist() {
	    // Arrange
	    when(customerRepository.existsByCf(anyString())).thenReturn(false);
	    when(customerMapper.toEntity(any(CustomerDto.class))).thenReturn(customer);
	    when(customerRepository.save(any(Customer.class))).thenReturn(customer);
	    when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

	    // Act
	    CustomerDto savedCustomer = customerService.saveCustomer(customerDto);

	    // Assert
	    assertNotNull(savedCustomer);
	    assertEquals("John Doe", savedCustomer.getName());
	    verify(customerRepository).existsByCf(customerDto.getCf());
	    verify(customerRepository).save(customer);
	    verify(customerMapper).toDto(customer);
	}

	@Test
	void saveCustomer_ShouldThrowException_WhenCustomerAlreadyExists() {
	    // Arrange
	    when(customerRepository.existsByCf(anyString())).thenReturn(true);

	    // Act & Assert
	    assertThrows(CustomerAlreadyExistsException.class, () -> customerService.saveCustomer(customerDto));
	}

	//----------->GET CUSTOMER BY ID<-----------

	@Test
	void getCustomerById_ShouldReturnCustomerDto_WhenCustomerExists() {
	    // Arrange
	    when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
	    when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

	    // Act
	    CustomerDto retrievedCustomer = customerService.getCustomerById(1L);

	    // Assert
	    assertNotNull(retrievedCustomer);
	    assertEquals("John Doe", retrievedCustomer.getName());
	    verify(customerRepository).findById(1L);
	    verify(customerMapper).toDto(customer);
	}

	@Test
	void getCustomerById_ShouldThrowException_WhenCustomerNotFound() {
	    // Arrange
	    when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

	    // Act & Assert
	    assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(1L));
	}

	//----------->GET ALL CUSTOMERS<-----------

	@Test
	void getAllCustomers_ShouldReturnCustomerList_WhenCustomersExist() {
	    // Arrange
	    List<Customer> customers = List.of(customer);
	    when(customerRepository.findAll()).thenReturn(customers);
	    when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

	    // Act
	    List<CustomerDto> customersDto = customerService.getAllCustomers();

	    // Assert
	    assertNotNull(customersDto);
	    assertEquals(1, customersDto.size());
	    assertEquals("John Doe", customersDto.get(0).getName());
	    verify(customerRepository).findAll();
	    verify(customerMapper).toDto(customer);
	}

	//----------->UPDATE CUSTOMER<-----------

	@Test
	void updateCustomer_ShouldReturnUpdatedCustomerDto_WhenCustomerExists() {
	    // Arrange
	    when(customerRepository.findByCf(anyString())).thenReturn(customer);
	    when(customerRepository.save(any(Customer.class))).thenReturn(customer);
	    when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

	    // Act
	    CustomerDto updatedCustomer = customerService.updateCustomer(customerDto);

	    // Assert
	    assertNotNull(updatedCustomer);
	    assertEquals("John Doe", updatedCustomer.getName());
	    verify(customerRepository).findByCf(customerDto.getCf());
	    verify(customerRepository).save(customer);
	    verify(customerMapper).toDto(customer);
	}

	@Test
	void updateCustomer_ShouldThrowException_WhenCustomerNotFound() {
	    // Arrange
	    when(customerRepository.findByCf(anyString())).thenReturn(null);

	    // Act & Assert
	    assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(customerDto));
	}

	//----------->DELETE CUSTOMER<-----------

	@Test
	void deleteCustomer_ShouldReturnCustomerDto_WhenCustomerExists() {
	    // Arrange
	    when(customerRepository.findByCf(anyString())).thenReturn(customer);
	    when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

	    // Act
	    CustomerDto deletedCustomer = customerService.deleteCustomer(customerDto.getCf());

	    // Assert
	    assertNotNull(deletedCustomer);
	    assertEquals("John Doe", deletedCustomer.getName());
	    verify(customerRepository).delete(customer);
	}
}
