package com.alaia.pharmX.order;

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
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("com.alaia.pharmX.models")
@EnableJpaRepositories(basePackages = "com.alaia.pharmX.repositories")
class OrderServiceAddLineTest {

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
    private AvailableQuantityProduct availableQuantity;

    @BeforeEach
    void setUp() {
        orderLineMapper = new OrderLineMapper();
        orderMapper = new OrderMapper(orderLineMapper);
        orderService = new OrderServiceImp(productRepository, stockService, customerRepository, orderMapper, orderLineMapper, orderRepository, orderLineRepository);
    }

    @Test
    void addLine_NewLine_Success() {
        // Arrange
    	String orderCodeRequest = "ORD-CF-999";
        OrderLineDto request = new OrderLineDto(0L, "ABC123", 5, null, LineOrderType.OPEN);
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        // Act
        OrderDto result = orderService.addLine(orderCodeRequest, request);

        // Assert
        assertNotNull(result);
        assertThat(result.getOrderLines(), hasSize(1));
        assertTrue(
            result.getOrderLines().stream().anyMatch(line -> line.getNationalCode().equals("ABC123"))
        );
        assertTrue(
            result.getOrderLines().iterator().next().getLineNumber().startsWith("ORDLINE-")
        );

        verify(stockService).reserveQuantity(argThat(op ->
            op.getNationalCode().equals("ABC123")
                && op.getReferenceType().equals("ORDER")
                && op.getReferenceId() > 0
                && op.getType() == MovementType.ORDER_ALLOCATION
                && op.getQuantity() == 5
        ));
        verifyNoMoreInteractions(stockService);
    }

	@Test
	void addLine_UpdateExistingLine_Success() {
		// Arrange
		String orderCodeRequest = "ORD-CF-123";
		OrderLineDto request = new OrderLineDto(0L, "ABC123", 3, null, LineOrderType.OPEN);
		when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

		// Act
        OrderDto result = orderService.addLine(orderCodeRequest, request);

        // Assert
        assertNotNull(result);
        assertThat(result.getOrderLines(), hasSize(1));

        assertTrue(
                result.getOrderLines().stream().anyMatch(line -> line.getNationalCode().equals("ABC123"))
        );
        assertTrue(
                result.getOrderLines().iterator().next().getLineNumber().startsWith("ORDLINE-")
        );

        assertEquals(8, result.getOrderLines().iterator().next().getQuantity());

        verify(stockService).reserveQuantity(argThat(op ->
        op.getNationalCode().equals("ABC123")
        && op.getReferenceType().equals("ORDER")
        && op.getReferenceId() > 0
        && op.getType() == MovementType.ORDER_ALLOCATION
        && op.getQuantity() == 3
        		));
        verifyNoMoreInteractions(stockService);
	}


	@Test
	void addLine_ProductNotFound_ThrowsException() {
		// Arrange
		String orderCodeRequest = "ORD-CF-123";
		OrderLineDto request = new OrderLineDto(0L, "NO_VALID", 3, null, LineOrderType.OPEN);

		// Act & Assert
		assertThrows(ProductNotFoundException.class, () ->
		orderService.addLine(orderCodeRequest, request));
		verify(stockService, never()).reserveQuantity(any());
	}

	@Test
	void addLine_OrderNotFound_ThrowsException() {
		// Arrange
		String orderCodeRequest = "ORD-CF-NULL";
		OrderLineDto request = new OrderLineDto(0L, "ABC123", 3, null, LineOrderType.OPEN);

		// Act & Assert
		assertThrows(OrderNotFoundException.class, () ->
		orderService.addLine(orderCodeRequest, request));
		verify(stockService, never()).reserveQuantity(any());
	}

	@Test
	void addLine_OrderNotOpen_ThrowsException() {
		// Arrange
		String orderCodeRequest = "ORD-CF-NOP";
		OrderLineDto request = new OrderLineDto(0L, "ABC123", 3, null, LineOrderType.OPEN);

		// Act & Assert
		assertThrows(InvalidUpdateQuantityException.class, () ->
		orderService.addLine(orderCodeRequest, request));
		verify(stockService, never()).reserveQuantity(any());
	}


	@Test
	void addLine_StockNotAvailable_ThrowsException() {
		// Arrange
		String orderCodeRequest = "ORD-CF-123";
		OrderLineDto request = new OrderLineDto(0L, "ABC123", 50, null, LineOrderType.OPEN);


		doThrow(new StockNotAvailableException("Stock not available")).when(stockService).reserveQuantity(any(StockOperation.class));

		// Act & Assert
		assertThrows(StockNotAvailableException.class, () ->
		orderService.addLine(orderCodeRequest, request));
	}

	@Test
	void addLine_ProductOutOfStock_ThrowsException() {
		// Arrange
		String orderCodeRequest = "ORD-CF-123";
		OrderLineDto request = new OrderLineDto(0L, "ABC123", 5, null, LineOrderType.OPEN);
		doThrow(new ProductOutOfStockException("Product out of stock")).when(stockService).reserveQuantity(any(StockOperation.class));

		// Act & Assert
		assertThrows(ProductOutOfStockException.class, () ->
		orderService.addLine(orderCodeRequest, request));
	}

}