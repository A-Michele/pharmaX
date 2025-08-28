package com.alaia.pharmX.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteCustomerWithOpenOrdersException;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.StateNotFoundException;
import com.alaia.pharmX.mappers.ContactMapper;
import com.alaia.pharmX.mappers.CustomerMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.Contact;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.servicesImpl.CustomerServiceImp;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private CustomerMapper customerMapper;

	@Mock
	private ContactMapper contactMapper;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderMapper orderMapper;

	@InjectMocks
	private CustomerServiceImp customerService;

	@InjectMocks
	private OrderServiceImp orderService;

	private CustomerDto customerDto;
	private Customer customer;
	private ContactDto contactDto;

	private ContractUpdateDto contractUpdateDto;
	private AddressUpdateDto addressUpdateDto;

	@BeforeEach
	void setUp() {
	    contactDto = new ContactDto(1L, "john@example.com", "1234567890");
	    customerDto = new CustomerDto(1L, "John Doe", "123 Street", "456 Avenue", contactDto, "1234567890123456");

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

	// -----------> GET CUSTOMER BY EMAIL <-----------

	@Test
	void getCustomerByEmail_ShouldThrow_WhenEmailIsNull() {
	    // Arrange & Act & Assert
	    assertThrows(IllegalArgumentException.class, () -> customerService.getCustomerByEmail(null));

	    // Verify: nessuna chiamata a repository/mapper
	    verifyNoInteractions(customerRepository, customerMapper, contactMapper);
	}

	@Test
	void getCustomerByEmail_ShouldReturnDto_WhenFound() {
	    // Arrange
	    // La lista contiene il customer preparato in setUp() con email "john@example.com"
	    when(customerRepository.findAll()).thenReturn(List.of(customer));
	    when(customerMapper.toDto(customer)).thenReturn(customerDto);

	    // Act
	    CustomerDto result = customerService.getCustomerByEmail("john@example.com");

	    // Assert
	    assertNotNull(result);
	    assertEquals(1L, result.getId());
	    assertEquals("John Doe", result.getName());
	    assertEquals("john@example.com", result.getContacts().getEmail());

	    // Verify: una sola mappatura del match, nessun’altra interazione
	    verify(customerRepository).findAll();
	    verify(customerMapper).toDto(customer);
	    verifyNoMoreInteractions(customerRepository, customerMapper);
	    verifyNoInteractions(contactMapper);
	}

	void getCustomerByEmail_ShouldThrow_WhenNotFound() {
	    // Arrange: email non presente
	    when(customerRepository.findAll()).thenReturn(List.of(customer));

	    // Act & Assert
	    assertThrows(CustomerNotFoundException.class,
	            () -> customerService.getCustomerByEmail("absent@example.com"));

	    // Verify
	    verify(customerRepository).findAll();
	    // Nessun mapping perché nessun match
	    verifyNoInteractions(customerMapper, contactMapper);
	}

	@Test
	void getCustomerByEmail_ShouldIgnoreNullContactsAndNullEmails() {
	    // Arrange
	    Customer noContacts = new Customer(2L, "No Contacts", "A", "B", null, "999");
	    Customer nullEmail = new Customer(3L, "Null Email", "A", "B", new Contact(0, null, "000"), "888");

	    when(customerRepository.findAll()).thenReturn(List.of(noContacts, nullEmail));

	    // Act & Assert
	    assertThrows(CustomerNotFoundException.class,
	            () -> customerService.getCustomerByEmail("john@example.com"));

	    // Verify
	    verify(customerRepository).findAll();
	    verifyNoInteractions(customerMapper, contactMapper);
	}

	@Test
	void getCustomerByEmail_ShouldBeCaseSensitive_AsCurrentlyImplemented() {
	    when(customerRepository.findAll()).thenReturn(List.of(customer));

	    // Act & Assert: cambia il case → non deve trovare
	    assertThrows(CustomerNotFoundException.class,
	            () -> customerService.getCustomerByEmail("John@Example.com"));

	    verify(customerRepository).findAll();
	    verifyNoInteractions(customerMapper, contactMapper);
	}

	// -----------> GET ORDERS BY CF <-----------

	@Test
	void getOrdersByCF_ShouldThrow_WhenCustomerNotFound() {
	    // Arrange
	    String cf = "RSSMRA85M01H501Z";
	    when(customerRepository.findByCf(cf)).thenReturn(null);

	    // Act & Assert
	    assertThrows(CustomerNotFoundException.class, () -> customerService.getOrdersByCF(cf));

	    // Verify
	    verify(customerRepository).findByCf(cf);
	    verifyNoInteractions(orderRepository, orderMapper);
	}

	@Test
	void getOrdersByCF_ShouldReturnMappedList_WhenCustomerExists() {
	    // Arrange
	    String cf = "RSSMRA85M01H501Z";
	    Customer existing = new Customer();
	    existing.setCf(cf);

	    Order o1 = new Order(); o1.setId(1L); o1.setCf(cf); o1.setState(State.PENDING);
	    Order o2 = new Order(); o2.setId(2L); o2.setCf(cf); o2.setState(State.SHIPPING);

	    OrderDto d1 = new OrderDto(); d1.setId(1L); d1.setCf(cf); d1.setState(State.PENDING);
	    OrderDto d2 = new OrderDto(); d2.setId(2L); d2.setCf(cf); d2.setState(State.SHIPPING);

	    when(customerRepository.findByCf(cf)).thenReturn(existing);
	    when(orderRepository.findByCf(cf)).thenReturn(List.of(o1, o2));
	    when(orderMapper.toDto(o1)).thenReturn(d1);
	    when(orderMapper.toDto(o2)).thenReturn(d2);

	    // Act
	    List<OrderDto> result = customerService.getOrdersByCF(cf);

	    // Assert
	    assertNotNull(result);
	    assertEquals(2, result.size());
	    assertEquals(1L, result.get(0).getId());
	    assertEquals(2L, result.get(1).getId());

	    verify(customerRepository).findByCf(cf);
	    verify(orderRepository).findByCf(cf);
	    verify(orderMapper).toDto(o1);
	    verify(orderMapper).toDto(o2);
	    verifyNoMoreInteractions(customerRepository, orderRepository, orderMapper);
	}

	// -----------> GET ORDERS BY CF AND TYPE (CustomerService) <-----------

	@Test
	void getOrdersByCFAndType_ShouldThrow_WhenCustomerNotFound() {
	    // Arrange
	    String cf = "RSSMRA85M01H501Z";
	    when(customerRepository.findByCf(cf)).thenReturn(null);

	    // Act & Assert
	    assertThrows(CustomerNotFoundException.class,
	            () -> customerService.getOrdersByCFAndType(cf, "PENDING"));

	    // Verify
	    verify(customerRepository).findByCf(cf);
	    verifyNoInteractions(orderRepository, orderMapper);
	}

	@Test
	void getOrdersByCFAndType_ShouldThrow_WhenTypeInvalid() {
	    // Arrange
	    String cf = "RSSMRA85M01H501Z";
	    Customer existing = new Customer(); existing.setCf(cf);
	    when(customerRepository.findByCf(cf)).thenReturn(existing);

	    // Act & Assert
	    assertThrows(StateNotFoundException.class,
	            () -> customerService.getOrdersByCFAndType(cf, "NOT_A_STATE"));

	    // Verify: non si interroga OrderRepository
	    verify(customerRepository).findByCf(cf);
	    verifyNoInteractions(orderRepository, orderMapper);
	}

	@Test
	void getOrdersByCFAndType_ShouldReturnList_WhenValid_CaseInsensitive() {
	    // Arrange
	    String cf = "RSSMRA85M01H501Z";
	    Customer existing = new Customer(); existing.setCf(cf);

	    Order o1 = new Order(); o1.setId(1L); o1.setCf(cf); o1.setState(State.PENDING);
	    Order o2 = new Order(); o2.setId(2L); o2.setCf(cf); o2.setState(State.PENDING);

	    OrderDto d1 = new OrderDto(); d1.setId(1L); d1.setCf(cf); d1.setState(State.PENDING);
	    OrderDto d2 = new OrderDto(); d2.setId(2L); d2.setCf(cf); d2.setState(State.PENDING);

	    when(customerRepository.findByCf(cf)).thenReturn(existing);
	    when(orderRepository.findByCfAndState(cf, State.PENDING)).thenReturn(List.of(o1, o2));
	    when(orderMapper.toDto(o1)).thenReturn(d1);
	    when(orderMapper.toDto(o2)).thenReturn(d2);

	    // Act
	    List<OrderDto> result = customerService.getOrdersByCFAndType(cf, "pending"); // minuscolo

	    // Assert
	    assertNotNull(result);
	    assertEquals(2, result.size());
	    assertEquals(State.PENDING, result.get(0).getState());

	    // Verify
	    verify(customerRepository).findByCf(cf);
	    verify(orderRepository).findByCfAndState(cf, State.PENDING);
	    verify(orderMapper).toDto(o1);
	    verify(orderMapper).toDto(o2);
	    verifyNoMoreInteractions(customerRepository, orderRepository, orderMapper);
	}

	// -----------> DELETE CUSTOMER SAFELY <-----------

	@Test
	void deleteCustomerSafely_ShouldThrow_WhenCustomerNotFound() {
		// Arrange
		String cf = "RSSMRA85M01H501Z";
		when(customerRepository.findByCf(cf)).thenReturn(null);

		// Act & Assert
		CustomerNotFoundException ex = assertThrows(
				CustomerNotFoundException.class,
				() -> customerService.deleteCustomerSafely(cf)
				);
		assertTrue(ex.getMessage().contains("Customer not found with CF: " + cf));

		// Verify: nessun accesso agli ordini, nessuna delete
		verify(customerRepository).findByCf(cf);
		verifyNoInteractions(orderRepository, customerMapper);
		verify(customerRepository, never()).delete(any());
	}

	@Test
	void deleteCustomerSafely_ShouldDeleteAndReturnDto_WhenNoOrders() {
		// Arrange
		String cf = customer.getCf();
		when(customerRepository.findByCf(cf)).thenReturn(customer);
		when(orderRepository.findByCf(cf)).thenReturn(List.of()); // nessun ordine
		when(customerMapper.toDto(customer)).thenReturn(customerDto);

		// Act
		CustomerDto result = customerService.deleteCustomerSafely(cf);

		// Assert
		assertNotNull(result);
		assertEquals(customerDto, result);

		// Verify: cancellazione effettuata, mappatura effettuata
		verify(customerRepository).findByCf(cf);
		verify(orderRepository).findByCf(cf);
		verify(customerRepository).delete(customer);
		verify(customerMapper).toDto(customer);
		verifyNoMoreInteractions(customerRepository, orderRepository, customerMapper);
	}

	@Test
	void deleteCustomerSafely_ShouldDeleteAndReturnDto_WhenOnlyCompletedOrCanceledOrders() {
		// Arrange
		String cf = customer.getCf();

		Order completed = new Order();
		completed.setId(10L);
		completed.setCf(cf);
		completed.setState(State.COMPLETED);
		completed.setCode("COMPLETED-10");

		Order canceled = new Order();
		canceled.setId(11L);
		canceled.setCf(cf);
		canceled.setState(State.CANCELED);
		canceled.setCode("CANCELED-11");

		when(customerRepository.findByCf(cf)).thenReturn(customer);
		when(orderRepository.findByCf(cf)).thenReturn(List.of(completed, canceled));
		when(customerMapper.toDto(customer)).thenReturn(customerDto);

		// Act
		CustomerDto result = customerService.deleteCustomerSafely(cf);

		// Assert
		assertNotNull(result);
		assertEquals(customerDto, result);

		// Verify: cancellazione consentita
		verify(customerRepository).findByCf(cf);
		verify(orderRepository).findByCf(cf);
		verify(customerRepository).delete(customer);
		verify(customerMapper).toDto(customer);
		verifyNoMoreInteractions(customerRepository, orderRepository, customerMapper);
	}

	@Test
	void deleteCustomerSafely_ShouldThrow_WhenBlockingOrderExists() {
		// Arrange
		String cf = customer.getCf();

		Order pending = new Order();
		pending.setId(20L);
		pending.setCf(cf);
		pending.setState(State.PENDING); // stato “aperto”
		pending.setCode("PEND-20");

		Order shipping = new Order();
		shipping.setId(21L);
		shipping.setCf(cf);
		shipping.setState(State.SHIPPING); // altro stato “aperto”
		shipping.setCode("SHIP-21");

		when(customerRepository.findByCf(cf)).thenReturn(customer);
		when(orderRepository.findByCf(cf)).thenReturn(List.of(pending, shipping));

		// Act & Assert
		CannotDeleteCustomerWithOpenOrdersException ex = assertThrows(
				CannotDeleteCustomerWithOpenOrdersException.class,
				() -> customerService.deleteCustomerSafely(cf)
				);

		assertTrue(ex.getMessage().contains("Cannot delete customer with CF: " + cf));

		assertTrue(ex.getMessage().contains("Product present in the order: " + pending.getCode()));
		assertTrue(ex.getMessage().contains(", with state: " + pending.getState()));

		verify(customerRepository).findByCf(cf);
		verify(orderRepository).findByCf(cf);
		verify(customerRepository, never()).delete(any());
		verifyNoInteractions(customerMapper);
	}
}