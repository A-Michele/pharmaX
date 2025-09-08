package com.alaia.pharmX.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.junit.jupiter.api.Test;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteOrderWithNoStateOpenException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("com.alaia.pharmX.models")
@EnableJpaRepositories(basePackages = "com.alaia.pharmX.repositories")
class OrderServiceDeleteOrderTest {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderLineRepository orderLineRepository;

	@Mock
	private StockService stockService;

	private OrderServiceImp orderService;
	private OrderLineMapper orderLineMapper;
	private OrderMapper orderMapper;

	@BeforeEach
	void setUp() {
		orderLineMapper = new OrderLineMapper();
		orderMapper = new OrderMapper(orderLineMapper);
		orderService = new OrderServiceImp(productRepository, stockService, customerRepository, orderMapper, orderLineMapper, orderRepository, orderLineRepository);
	}

	@Test
	void deleteOrder_OrderNoStateOpen_ThrowsException() {
		// Arrange
		String orderCode = "ORD-CF-NOP";

		// Act & Assert
		assertThrows(CannotDeleteOrderWithNoStateOpenException.class, () ->
		orderService.deleteOrder(orderCode));
		Order order = orderRepository.findByCode(orderCode);
		assertNotNull(order);
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
	}

	@Test
	void deleteOrder_Success() {
		// Arrange
		String orderCode = "ORD-CF-123";
		OrderDto request = orderService.getOrderByCode(orderCode);

		// Act
		OrderDto result = orderService.deleteOrder(orderCode);

		// Assert
		assertNotNull(result);
		assertEquals(State.CANCELED, result.getState());

		assertThat(request)
        .usingRecursiveComparison()
        .ignoringFields("orderLines","state")
        .isEqualTo(result);

		assertThat(result.getOrderLines(), hasSize(1));
		assertTrue(
	            result.getOrderLines().stream().anyMatch(line -> line.getType().equals(LineOrderType.CANCELED))
	    );

		verify(stockService).unReserveQuantityOnDeleteOrCanceled(argThat(op ->
		op.getNationalCode().equals("ABC123") &&
		op.getReferenceType().equals("ORDER") &&
		op.getReferenceId() == 1L &&
		op.getType() == MovementType.RETURN &&
		op.getQuantity() == 5
				));
		verifyNoMoreInteractions(stockService);
	}

	@Test
	void deleteOrder_NoLines_Success() {
		// Arrange
		String orderCode = "ORD-CF-999";
		OrderDto request = orderService.getOrderByCode(orderCode);

		// Act
		OrderDto result = orderService.deleteOrder(orderCode);

		// Assert
		assertNotNull(result);
		assertThat(request)
        .usingRecursiveComparison()
        .ignoringFields("state")
        .isEqualTo(result);

		assertEquals(State.CANCELED, result.getState());
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
	}

	@Test
	void deleteOrder_OrderNotFound_ThrowsException() {
		// Arrange
		String orderCode = "ORD-NOTFOUND";

		// Act & Assert
		assertThrows(OrderNotFoundException.class, () ->
		orderService.deleteOrder(orderCode));
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
	}

	@Test
	void deleteOrder_InvalidState_ThrowsException() {
		// Arrange
		String orderCode = "ORD-CF-PICKING";
		OrderDto request = orderService.getOrderByCode(orderCode);

		// Act & Assert
		assertThrows(CannotDeleteOrderWithNoStateOpenException.class, () ->
		orderService.deleteOrder(orderCode));
		Order order = orderRepository.findByCode(orderCode);

		assertThat(request)
        .usingRecursiveComparison()
        .ignoringFields("orderLines","state")
        .isEqualTo(order);

		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
	}

	@Test
	void deleteOrder_ProductOutOfStock_ThrowsException() {
		// Arrange
		String orderCode = "ORD-CF-123";
		doThrow(new ProductOutOfStockException("Product out of stock")).when(stockService).unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class));

		// Act & Assert
		assertThrows(ProductOutOfStockException.class, () ->
		orderService.deleteOrder(orderCode));
	}
}
