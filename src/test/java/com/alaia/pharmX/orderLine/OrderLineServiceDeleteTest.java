package com.alaia.pharmX.orderLine;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderLineServiceImp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class OrderLineServiceDeleteTest {

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private StockService stockService;

    @Mock
    private OrderLineMapper orderLineMapper;

    @InjectMocks
    private OrderLineServiceImp orderService;

    private Order order;
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
    }

    @Test
    void delete_Success() {
        // Arrange
        long orderLineId = 1L;
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(orderLineMapper.toDto(orderLine)).thenReturn(orderLineDto);
        doNothing().when(orderLineRepository).delete(orderLine);

        // Act
        OrderLineDto result = orderService.delete(orderLineId);

        // Assert
        assertNotNull(result);
        assertEquals(orderLineDto, result);
        assertFalse(order.getOrderLines().contains(orderLine));
        verify(stockService).unReserveQuantityOnDeleteOrCanceled(argThat(op ->
            op.getNationalCode().equals("ABC123") &&
            op.getReferenceType().equals("ORDER") &&
            op.getReferenceId() == 1L &&
            op.getType() == MovementType.RETURN &&
            op.getQuantity() == 5
        ));
        verify(orderLineRepository).delete(orderLine);
    }

    @Test
    void delete_OrderLinesNull_Success() {
        // Arrange
        long orderLineId = 1L;
        order.setOrderLines(null);
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(orderLineMapper.toDto(orderLine)).thenReturn(orderLineDto);
        doNothing().when(orderLineRepository).delete(orderLine);

        // Act
        OrderLineDto result = orderService.delete(orderLineId);

        // Assert
        assertNotNull(result);
        assertEquals(orderLineDto, result);
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository).delete(orderLine);
        verify(orderLineMapper).toDto(orderLine);
    }

    @Test
    void delete_OrderLineNotFound_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderLineNotFoundException.class, () ->
                orderService.delete(orderLineId));
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).delete(any());
        verify(orderLineMapper, never()).toDto(any());
    }

    @Test
    void delete_OrderNotFound_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        orderLine.setOrder(null);
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

        // Act & Assert
        assertThrows(InvalidOrderOperationException.class, () ->
                orderService.delete(orderLineId));
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).delete(any());
        verify(orderLineMapper, never()).toDto(any());
    }

    @Test
    void delete_OrderNotOpen_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        order.setState(State.PENDING);
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

        // Act & Assert
        assertThrows(InvalidUpdateQuantityException.class, () ->
                orderService.delete(orderLineId));
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).delete(any());
        verify(orderLineMapper, never()).toDto(any());
    }
}
