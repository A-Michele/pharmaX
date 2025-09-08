package com.alaia.pharmX.order;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
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
import com.alaia.pharmX.servicesImpl.order.OrderLineServiceImp;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("com.alaia.pharmX.models")
@EnableJpaRepositories(basePackages = "com.alaia.pharmX.repositories")
class OrderServiceRemoveLineTest {

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
    private OrderLineServiceImp orderLineService;
    private OrderLineMapper orderLineMapper;
    private OrderMapper orderMapper;
    private AvailableQuantityProduct availableQuantity;

    @BeforeEach
    void setUp() {
        orderLineMapper = new OrderLineMapper();
        orderMapper = new OrderMapper(orderLineMapper);
        orderService = new OrderServiceImp(productRepository, stockService, customerRepository, orderMapper, orderLineMapper, orderRepository, orderLineRepository);
        orderLineService= new OrderLineServiceImp(productRepository, orderLineMapper, orderRepository, orderLineRepository, stockService);
    }

	@Test
	void removeLine_Success() {
		// Arrange
		String lineNumber = "ORDLINE-123456";
		OrderLineDto request = orderLineService.getByLineNumber(lineNumber);
		when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

		// Act
		OrderDto result = orderService.removeLine(request.getId());

		// Assert
		assertNotNull(result);
		assertFalse(result.getOrderLines().contains(request));
		verify(stockService).unReserveQuantityOnDeleteOrCanceled(argThat(op ->
		op.getNationalCode().equals("ABC123") &&
		op.getReferenceType().equals("ORDER") &&
		op.getReferenceId() == 1L &&
		op.getType() == MovementType.ADJUSTMENT &&
		op.getQuantity() == 5
				));
		verifyNoMoreInteractions(stockService);
	}

	@Test
	void removeLine_OrderLineNotFound_ThrowsException() {
		// Arrange
		long orderLineId = 99999L;

		// Act & Assert
		assertThrows(OrderLineNotFoundException.class, () ->
		orderService.removeLine(orderLineId));
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
	}

	@Test
	void removeLine_NoAssociatedOrder_ThrowsException() {
		// Arrange
		String lineNumber = "ORDLINE-NOORDER";
		OrderLineDto request = orderLineService.getByLineNumber(lineNumber);

		// Act & Assert
		assertThrows(InvalidOrderOperationException.class, () ->
		orderService.removeLine(request.getId()));
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
	}

	@Test
	void removeLine_OrderNotOpen_ThrowsException() {
		// Arrange
		String lineNumber = "ORDLINE-123465";
		OrderLineDto request = orderLineService.getByLineNumber(lineNumber);

		// Act & Assert
		assertThrows(InvalidOrderOperationException.class, () ->
		orderService.removeLine(request.getId()));
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
	}

	@Test
	void removeLine_ProductOutOfStock_ThrowsException() {
		// Arrange
		String lineNumber = "ORDLINE-123456";
		OrderLineDto request = orderLineService.getByLineNumber(lineNumber);

		doThrow(new ProductOutOfStockException("Product out of stock")).when(stockService).unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class));

		// Act & Assert
		assertThrows(ProductOutOfStockException.class, () ->
		orderService.removeLine(request.getId()));
	}

}
