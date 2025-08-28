package com.alaia.pharmX.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;

@ExtendWith(MockitoExtension.class)
public class OrderServiceRemoveLineTest {
	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderLineRepository orderLineRepository;

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
	private OrderLine orderLine;
	private OrderLineDto orderLineDto;

	@BeforeEach
	void setUp() {

		orderLineDto = new OrderLineDto();
		orderLineDto.setId(1L);
		orderLineDto.setNationalCode("ABC123");
		orderLineDto.setQuantity(5);
		orderLineDto.setLineNumber("ORDLINE-123456");
		orderLineDto.setType(LineOrderType.OPEN);

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

		orderLine.setOrder(order);

		orderDto = new OrderDto();
		orderDto.setId(1L);
		orderDto.setCode("ORD001");
		orderDto.setState(State.OPEN);
		orderDto.setCf("CF123");
		orderDto.setDate(LocalDateTime.now());
		orderDto.setOrderLines(Set.of());
	}

	@Test
	void removeLine_Success() {
		// Arrange
		long orderLineId = 1L;
		when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
		when(orderLineMapper.toDto(orderLine)).thenReturn(orderLineDto);
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(orderMapper.toDto(order)).thenReturn(orderDto);

		// Act
		OrderDto result = orderService.removeLine(orderLineId);

		// Assert
		assertNotNull(result);
		assertEquals(orderDto, result);
		assertFalse(order.getOrderLines().contains(orderLine));
		verify(stockService).unReserveQuantityOnDeleteOrCanceled(argThat(op ->
		op.getNationalCode().equals("ABC123") &&
		op.getReferenceType().equals("ORDER") &&
		op.getReferenceId() == 1L &&
		op.getType() == MovementType.ADJUSTMENT &&
		op.getQuantity() == 5
				));
		verify(orderLineRepository).delete(orderLine);
		verify(orderRepository).save(any(Order.class));
		verify(orderMapper).toDto(order);
	}

	@Test
	void removeLine_OrderLineNotFound_ThrowsException() {
		// Arrange
		long orderLineId = 1L;
		when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(OrderLineNotFoundException.class, () ->
		orderService.removeLine(orderLineId));
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
		verify(orderLineRepository, never()).delete(any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void removeLine_NoAssociatedOrder_ThrowsException() {
		// Arrange
		long orderLineId = 1L;
		orderLine.setOrder(null);
		when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

		// Act & Assert
		assertThrows(InvalidOrderOperationException.class, () ->
		orderService.removeLine(orderLineId));
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
		verify(orderLineRepository, never()).delete(any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void removeLine_OrderNotOpen_ThrowsException() {
		// Arrange
		long orderLineId = 1L;
		order.setState(State.PENDING);
		when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

		// Act & Assert
		assertThrows(InvalidUpdateQuantityException.class, () ->
		orderService.removeLine(orderLineId));
		verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
		verify(orderLineRepository, never()).delete(any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void removeLine_ProductOutOfStock_ThrowsException() {
		// Arrange
		long orderLineId = 1L;
		when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
		when(orderLineMapper.toDto(orderLine)).thenReturn(orderLineDto);
		doThrow(new ProductOutOfStockException("Product out of stock")).when(stockService).unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class));

		// Act & Assert
		assertThrows(ProductOutOfStockException.class, () ->
		orderService.removeLine(orderLineId));
		verify(orderLineRepository, never()).delete(any());
		verify(orderRepository, never()).save(any());
	}

}
