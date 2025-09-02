package com.alaia.pharmX.orderLine;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class OrderLineServiceUpdateQuantityTest {

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
    private AvailableQuantityProduct availableQuantity;

    @BeforeEach
    void setUp() {
        // Inizializzazione degli oggetti di test
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
        order.setOrderLines(new HashSet<>());

        orderLine.setOrder(order);

        availableQuantity = new AvailableQuantityProduct("ABC123", 10);
    }

    @Test
    void updateQuantity_IncreaseQuantity_Success() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 7;
        OrderLine updatedOrderLine = new OrderLine();
        updatedOrderLine.setId(1L);
        updatedOrderLine.setNationalCode("ABC123");
        updatedOrderLine.setQuantity(7);
        updatedOrderLine.setLineNumber("ORDLINE-123456");
        updatedOrderLine.setType(LineOrderType.OPEN);
        updatedOrderLine.setOrder(order);

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);
        when(orderLineMapper.toDto(any(OrderLine.class))).thenReturn(orderLineDto);
        when(orderLineRepository.save(orderLine)).thenReturn(updatedOrderLine);

        // Act
        OrderLineDto result = orderService.updateQuantity(orderLineId, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(orderLineDto, result);
        assertEquals(7, orderLine.getQuantity());
        verify(stockService).reserveQuantity(argThat(op ->
            op.getNationalCode().equals("ABC123") &&
            op.getReferenceType().equals("ORDER") &&
            op.getReferenceId() == 1L &&
            op.getType() == MovementType.ADJUSTMENT &&
            op.getQuantity() == 2
        ));
        verify(orderLineRepository).save(orderLine);
    }

    @Test
    void updateQuantity_DecreaseQuantity_Success() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 3;
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);
        when(orderLineMapper.toDto(any(OrderLine.class))).thenReturn(orderLineDto);
        when(orderLineRepository.save(orderLine)).thenReturn(orderLine);

        // Act
        OrderLineDto result = orderService.updateQuantity(orderLineId, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(orderLineDto, result);
        assertEquals(3, orderLine.getQuantity());
        verify(stockService).unReserveQuantityOnDeleteOrCanceled(argThat(op ->
            op.getNationalCode().equals("ABC123") &&
            op.getReferenceType().equals("ORDER") &&
            op.getReferenceId() == 1L &&
            op.getType() == MovementType.ADJUSTMENT &&
            op.getQuantity() == 2
        ));
        verify(orderLineRepository).save(orderLine);
    }

    @Test
    void updateQuantity_SameQuantity_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 5;
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateQuantity(orderLineId, newQuantity));
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
        verify(orderLineMapper, never()).toDto(any());
    }

    @Test
    void updateQuantity_OrderLineNotFound_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderLineNotFoundException.class, () ->
                orderService.updateQuantity(orderLineId, 7));
        verify(stockService, never()).getAvailableQuantity(any());
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
        verify(orderLineMapper, never()).toDto(any());
    }

    @Test
    void updateQuantity_OrderNotOpen_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        order.setState(State.RELEASED);
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

        // Act & Assert
        assertThrows(InvalidUpdateQuantityException.class, () ->
                orderService.updateQuantity(orderLineId, 7));
        verify(stockService, never()).getAvailableQuantity(any());
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
        verify(orderLineMapper, never()).toDto(any());
    }

    @Test
    void updateQuantity_StockNotAvailable_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 7;
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(null);

        // Act & Assert
        assertThrows(StockNotAvailableException.class, () ->
                orderService.updateQuantity(orderLineId, newQuantity));
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
        verify(orderLineMapper, never()).toDto(any());
    }

    @Test
    void updateQuantity_QuantityNotAvailable_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 7;
        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(new AvailableQuantityProduct("ABC123", 1));

        // Act & Assert
        assertThrows(QuantityNotAvailableException.class, () ->
                orderService.updateQuantity(orderLineId, newQuantity));
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
        verify(orderLineMapper, never()).toDto(any());
    }
}
