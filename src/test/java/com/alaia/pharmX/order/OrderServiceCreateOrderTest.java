package com.alaia.pharmX.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.Contact;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;

@ExtendWith(MockitoExtension.class)
public class OrderServiceCreateOrderTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private StockService stockService;

	@Mock
	private OrderMapper orderMapper;

	@InjectMocks
	private OrderServiceImp orderService;

	private OrderDto orderDto;
	private Order order;
	private OrderLineDto orderLineDto;
	private Customer customer;
	private AvailableQuantityProduct availableQuantity;

	@BeforeEach
	void setUp() {

		customer = new Customer();
		customer.setId(1L);
		customer.setCf("CF123");
		customer.setName("Test Customer");
		customer.setShippingAddress("Via Test 123");
		customer.setBillingAddress("Via Test 456");

		customer.setContacts(new Contact(1L, "test@example.com", "1234567890"));

		orderLineDto = new OrderLineDto();
		orderLineDto.setId(1L);
		orderLineDto.setNationalCode("ABC123");
		orderLineDto.setQuantity(5);
		orderLineDto.setLineNumber("ORDLINE-123456");
		orderLineDto.setType(LineOrderType.OPEN);

		orderDto = new OrderDto();
		orderDto.setId(0L);
		orderDto.setCode("ORD001");
		orderDto.setState(null);
		orderDto.setCf("CF123");
		orderDto.setDate(LocalDateTime.now());
		orderDto.setOrderLines(Set.of(orderLineDto));

		order = new Order();
		order.setId(1L);
		order.setCode("ORD001");
		order.setState(State.OPEN);
		order.setCf("CF123");
		order.setDate(LocalDateTime.now());
		order.setOrderLines(Set.of());

		availableQuantity = new AvailableQuantityProduct("ABC123", 10);
	}

	@Test
	void createOrder_Success() {
		// Arrange
		when(orderRepository.existsByCode("ORD001")).thenReturn(false);
		when(customerRepository.findByCf("CF123")).thenReturn(customer);
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
		when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);
		when(orderMapper.toEntity(orderDto)).thenReturn(order);//Si puo togliere?
		when(orderRepository.saveAndFlush(any(Order.class))).thenReturn(order);
		when(orderMapper.toDto(order)).thenReturn(orderDto);//Si puo togliere?

		// Act
		OrderDto result = orderService.createOrder(orderDto);

		// Assert
		assertNotNull(result);
		assertEquals(orderDto, result);
		assertEquals(State.OPEN, result.getState());//Eliminare
		verify(orderRepository).saveAndFlush(any(Order.class));
		verify(stockService).reserveQuantity(argThat(op ->
		op.getNationalCode().equals("ABC123") &&
		op.getReferenceType().equals("ORDER") &&
		op.getReferenceId() == 1L &&
		op.getType() == MovementType.ORDER_ALLOCATION &&
		op.getQuantity() == 5
				));
		verify(orderMapper).toDto(order);
	}

	@Test
	void createOrder_OrderAlreadyExists_ThrowsException() {
		// Arrange
		when(orderRepository.existsByCode("ORD001")).thenReturn(true);

		// Act & Assert
		assertThrows(OrderAlreadyExistsException.class, () ->
		orderService.createOrder(orderDto));
		verify(customerRepository, never()).findByCf(any());
		verify(productRepository, never()).existsByNationalCode(any());
		verify(stockService, never()).reserveQuantity(any());
		verify(orderRepository, never()).saveAndFlush(any());
	}

	@Test
	void createOrder_CustomerNotFound_ThrowsException() {
		// Arrange
		when(orderRepository.existsByCode("ORD001")).thenReturn(false);
		when(customerRepository.findByCf("CF123")).thenReturn(null);

		// Act & Assert
		assertThrows(CustomerNotFoundException.class, () ->
		orderService.createOrder(orderDto));
		verify(productRepository, never()).existsByNationalCode(any());
		verify(stockService, never()).reserveQuantity(any());
		verify(orderRepository, never()).saveAndFlush(any());
	}

	@Test
	void createOrder_ProductNotFound_ThrowsException() {
		// Arrange
		when(orderRepository.existsByCode("ORD001")).thenReturn(false);
		when(customerRepository.findByCf("CF123")).thenReturn(customer);
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(false);

		// Act & Assert
		assertThrows(ProductNotFoundException.class, () ->
		orderService.createOrder(orderDto));
		verify(stockService, never()).reserveQuantity(any());
		verify(orderRepository, never()).saveAndFlush(any());
	}

	@Test
    void createOrder_ProductOutOfStock_ThrowsException() {
        // Arrange
        when(orderRepository.existsByCode("ORD001")).thenReturn(false);
        when(customerRepository.findByCf("CF123")).thenReturn(customer);
        when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
        when(stockService.getAvailableQuantity("ABC123")).thenThrow(new ProductOutOfStockException("Product out of stock"));

        // Act & Assert
        assertThrows(ProductOutOfStockException.class, () ->
                orderService.createOrder(orderDto));
        verify(stockService, never()).reserveQuantity(any());
        verify(orderRepository, never()).saveAndFlush(any());
	}
}
