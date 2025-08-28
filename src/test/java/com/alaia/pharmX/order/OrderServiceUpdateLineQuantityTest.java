package com.alaia.pharmX.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUpdateLineQuantityTest {

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StockService stockService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderLineMapper orderLineMapper;

    @InjectMocks
    private OrderServiceImp orderServiceImp;

    private Order order;
    private OrderLine orderLine;
    private OrderDto orderDto;
    private OrderLineDto orderLineDto;
    private AvailableQuantityProduct availableQuantity;
    private StockOperation stockOperation;

    @BeforeEach
    void setUp() {

        order = new Order();
        order.setId(1L);
        order.setCode("ORD001");
        order.setState(State.OPEN);
        order.setCf("CF123");
        order.setDate(LocalDateTime.now());
        order.setOrderLines(Set.of());

        orderLine = new OrderLine();
        orderLine.setId(1L);
        orderLine.setNationalCode("ABC123");
        orderLine.setQuantity(5);
        orderLine.setLineNumber("ORDLINE-123456");
        orderLine.setType(LineOrderType.OPEN);
        orderLine.setOrder(order);

        orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setCode("ORD001");
        orderDto.setState(State.OPEN);
        orderDto.setCf("CF123");
        orderDto.setDate(LocalDateTime.now());
        orderDto.setOrderLines(Set.of());

        orderLineDto = new OrderLineDto();
        orderLineDto.setId(1L);
        orderLineDto.setNationalCode("ABC123");
        orderLineDto.setQuantity(5);
        orderLineDto.setLineNumber("ORDLINE-123456");
        orderLineDto.setType(LineOrderType.OPEN);

        availableQuantity = new AvailableQuantityProduct("ABC123", 10);

        stockOperation = new StockOperation();
        stockOperation.setNationalCode("ABC123");
        stockOperation.setReferenceType("ORDER");
        stockOperation.setReferenceId(1L);
        stockOperation.setType(MovementType.ADJUSTMENT);
        stockOperation.setQuantity(2);
    }

    @Test
    void updateLineQuantity_IncreaseQuantity_Success() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 7;

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);
        when(orderLineMapper.toDto(orderLine)).thenReturn(orderLineDto);
        when(orderLineRepository.save(orderLine)).thenReturn(orderLine);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        // Act
        OrderDto result = orderServiceImp.updateLineQuantity(orderLineId, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(orderDto, result);
        assertEquals(newQuantity, orderLine.getQuantity());
        verify(stockService).reserveQuantity(argThat(op ->
            op.getNationalCode().equals("ABC123") &&
            op.getReferenceType().equals("ORDER") &&
            op.getReferenceId() == 1L &&
            op.getType() == MovementType.ADJUSTMENT &&
            op.getQuantity() == 2
        ));
        verify(orderLineRepository).save(orderLine);
        verify(orderRepository).findById(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    void updateLineQuantity_DecreaseQuantity_Success() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 3;

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);
        when(orderLineMapper.toDto(orderLine)).thenReturn(orderLineDto);
        when(orderLineRepository.save(orderLine)).thenReturn(orderLine);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        // Act
        OrderDto result = orderServiceImp.updateLineQuantity(orderLineId, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(orderDto, result);
        assertEquals(newQuantity, orderLine.getQuantity());
        verify(stockService).unReserveQuantityOnDeleteOrCanceled(argThat(op ->
            op.getNationalCode().equals("ABC123") &&
            op.getReferenceType().equals("ORDER") &&
            op.getReferenceId() == 1L &&
            op.getType() == MovementType.ADJUSTMENT &&
            op.getQuantity() == 2
        ));
        verify(orderLineRepository).save(orderLine);
        verify(orderRepository).findById(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    void updateLineQuantity_OrderLineNotFound_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 7;

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderLineNotFoundException.class, () ->
                orderServiceImp.updateLineQuantity(orderLineId, newQuantity));
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
    }

    @Test
    void updateLineQuantity_OrderNotOpen_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 7;
        order.setState(State.PENDING);

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

        // Act & Assert
        assertThrows(InvalidUpdateQuantityException.class, () ->
                orderServiceImp.updateLineQuantity(orderLineId, newQuantity));
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
    }

    @Test
    void updateLineQuantity_QuantityNotAvailable_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 15;

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        // Act & Assert
        assertThrows(QuantityNotAvailableException.class, () ->
                orderServiceImp.updateLineQuantity(orderLineId, newQuantity));
        verify(orderLineRepository, never()).save(any());
    }

    @Test
    void updateLineQuantity_SameQuantity_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 5;

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderServiceImp.updateLineQuantity(orderLineId, newQuantity));
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
        verify(orderLineRepository, never()).save(any());
    }

    @Test
    void updateLineQuantity_OrderNotFound_ThrowsException() {
        // Arrange
        long orderLineId = 1L;
        int newQuantity = 7;

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);
        when(orderLineMapper.toDto(orderLine)).thenReturn(orderLineDto);
        when(orderLineRepository.save(orderLine)).thenReturn(orderLine);
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () ->
                orderServiceImp.updateLineQuantity(orderLineId, newQuantity));
        verify(orderLineRepository).save(orderLine);
        verify(stockService).reserveQuantity(any(StockOperation.class));
    }
}
