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
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;

@ExtendWith(MockitoExtension.class)
class OrderServiceAddLineTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderLineRepository orderLineRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private StockService stockService;

	@Mock
	private OrderMapper orderMapper;

	@Mock
	private OrderLineMapper orderLineMapper;

	@InjectMocks
	private OrderServiceImp orderService;

	private Order order;
	private OrderDto orderDto;
	private OrderLineDto orderLineDto;
	private OrderLine orderLine;

	@BeforeEach
	void setUp() {
		orderLineDto = new OrderLineDto();
		orderLineDto.setId(0L);
		orderLineDto.setNationalCode("ABC123");
		orderLineDto.setQuantity(5);
		orderLineDto.setLineNumber(null);
		orderLineDto.setType(null);

		orderLine = new OrderLine();
		orderLine.setId(1L);
		orderLine.setNationalCode("ABC123");
		orderLine.setQuantity(5);
		orderLine.setLineNumber("ORDLINE-123456");
		orderLine.setType(LineOrderType.OPEN);

		order = new Order();
		order.setId(1L);
		order.setCode("ORD001");
		order.setState(State.OPEN);
		order.setCf("CF123");
		order.setDate(LocalDateTime.now());
		order.setOrderLines(new HashSet<>(Set.of(orderLine)));

		orderDto = new OrderDto();
		orderDto.setId(1L);
		orderDto.setCode("ORD001");
		orderDto.setState(State.OPEN);
		orderDto.setCf("CF123");
		orderDto.setDate(LocalDateTime.now());
		orderDto.setOrderLines(Set.of());
	}

	@Test
	void addLine_NewLine_Success() {
		// Arrange
		String orderCode = "ORD001";
		order.setOrderLines(new HashSet<>());
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
		when(orderRepository.findByCode(orderCode)).thenReturn(order);
		when(orderLineMapper.toEntity(orderLineDto)).thenReturn(orderLine);
		when(orderLineRepository.save(any(OrderLine.class))).thenReturn(orderLine);
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(orderMapper.toDto(order)).thenReturn(orderDto);

		// Act
		OrderDto result = orderService.addLine(orderCode, orderLineDto);

		// Assert
		assertNotNull(result);
		assertEquals(orderDto, result);
		assertTrue(order.getOrderLines().contains(orderLine));
		assertEquals(LineOrderType.OPEN, orderLine.getType());
		assertTrue(orderLine.getLineNumber().startsWith("ORDLINE-"));
		verify(stockService).reserveQuantity(argThat(op ->
		op.getNationalCode().equals("ABC123") &&
		op.getReferenceType().equals("ORDER") &&
		op.getReferenceId() == 1L &&
		op.getType() == MovementType.ORDER_ALLOCATION &&
		op.getQuantity() == 5
				));
		verify(orderLineRepository).save(any(OrderLine.class));
		verify(orderRepository).save(any(Order.class));
		verify(orderMapper).toDto(order);
	}

	@Test
	void addLine_UpdateExistingLine_Success() {
		// Arrange
		String orderCode = "ORD001";
		orderLine.setQuantity(3);
		order.setOrderLines(new HashSet<>(Set.of(orderLine)));
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
		when(orderRepository.findByCode(orderCode)).thenReturn(order);
		when(orderLineRepository.save(orderLine)).thenReturn(orderLine);
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(orderMapper.toDto(order)).thenReturn(orderDto);

		// Act
		OrderDto result = orderService.addLine(orderCode, orderLineDto);

		// Assert
		assertNotNull(result);
		assertEquals(orderDto, result);
		assertEquals(8, orderLine.getQuantity()); // 3 + 5
		verify(stockService).reserveQuantity(argThat(op ->
		op.getNationalCode().equals("ABC123") &&
		op.getReferenceType().equals("ORDER") &&
		op.getReferenceId() == 1L &&
		op.getType() == MovementType.ORDER_ALLOCATION &&
		op.getQuantity() == 5
				));
		verify(orderLineRepository).save(orderLine);
		verify(orderRepository).save(any(Order.class));
		verify(orderMapper).toDto(order);
		verify(orderLineMapper, never()).toEntity(any());
	}

	@Test
	void addLine_ProductNotFound_ThrowsException() {
		// Arrange
		String orderCode = "ORD001";
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(false);

		// Act & Assert
		assertThrows(ProductNotFoundException.class, () ->
		orderService.addLine(orderCode, orderLineDto));
		verify(orderRepository, never()).findByCode(any());
		verify(stockService, never()).reserveQuantity(any());
		verify(orderLineRepository, never()).save(any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void addLine_OrderNotFound_ThrowsException() {
		// Arrange
		String orderCode = "ORD001";
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
		when(orderRepository.findByCode(orderCode)).thenReturn(null);

		// Act & Assert
		assertThrows(OrderNotFoundException.class, () ->
		orderService.addLine(orderCode, orderLineDto));
		verify(stockService, never()).reserveQuantity(any());
		verify(orderLineRepository, never()).save(any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void addLine_OrderNotOpen_ThrowsException() {
		// Arrange
		String orderCode = "ORD001";
		order.setState(State.PENDING);
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
		when(orderRepository.findByCode(orderCode)).thenReturn(order);

		// Act & Assert
		assertThrows(InvalidUpdateQuantityException.class, () ->
		orderService.addLine(orderCode, orderLineDto));
		verify(stockService, never()).reserveQuantity(any());
		verify(orderLineRepository, never()).save(any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void addLine_StockNotAvailable_ThrowsException() {
		// Arrange
		String orderCode = "ORD001";
		when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
		when(orderRepository.findByCode(orderCode)).thenReturn(order);
		doThrow(new StockNotAvailableException("Stock not available")).when(stockService).reserveQuantity(any(StockOperation.class));

		// Act & Assert
		assertThrows(StockNotAvailableException.class, () ->
		orderService.addLine(orderCode, orderLineDto));
		verify(orderLineRepository, never()).save(any());
		verify(orderRepository, never()).save(any());
	}

	 @Test
	    void addLine_ProductOutOfStock_ThrowsException() {
	        // Arrange
	        String orderCode = "ORD001";
	        when(productRepository.existsByNationalCode("ABC123")).thenReturn(true);
	        when(orderRepository.findByCode(orderCode)).thenReturn(order);
	        doThrow(new ProductOutOfStockException("Product out of stock")).when(stockService).reserveQuantity(any(StockOperation.class));

	        // Act & Assert
	        assertThrows(ProductOutOfStockException.class, () ->
	                orderService.addLine(orderCode, orderLineDto));
	        verify(orderLineRepository, never()).save(any());
	        verify(orderRepository, never()).save(any());
	    }
}
