package com.alaia.pharmX.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.OrderDto;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.mappers.OrderLineMapper;
import com.alaia.pharmX.mappers.OrderMapper;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.models.Order;
import com.alaia.pharmX.models.OrderLine;
import com.alaia.pharmX.models.State;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.OrderLineRepository;
import com.alaia.pharmX.repositories.OrderRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.servicesImpl.OrderServiceImp;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.InvalidOrderOperationException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderLineNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductNotFoundException;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderLineMapper orderLineMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @InjectMocks
    private OrderServiceImp service;

    private OrderDto baseDto;
    private Order mappedEntity;
    private Order savedEntity;
    private Customer customer;

    @BeforeEach
    void setUp() {
        baseDto = new OrderDto(1L, "OCODE", State.OPEN, "CF", null, new HashSet<>());
        mappedEntity = new Order();
        mappedEntity.setId(1L);
        mappedEntity.setCode("OCODE");
        mappedEntity.setState(State.OPEN);
        mappedEntity.setCf("CF");
        mappedEntity.setOrderLines(new HashSet<>());

        savedEntity = new Order();
        savedEntity.setId(1L);
        savedEntity.setCode("OCODE");
        savedEntity.setState(State.OPEN);
        savedEntity.setCf("CF");
        savedEntity.setOrderLines(new HashSet<>());

        customer = new Customer();
        customer.setCf("CF");
    }

    // ----------->CREATE ORDER<-----------

    @Test
    void createOrder_OrderAlreadyExists_ShouldThrow() {
        when(orderRepository.existsByCode("OCODE")).thenReturn(true);

        assertThrows(OrderAlreadyExistsException.class, () -> service.createOrder(baseDto));
        verify(orderRepository).existsByCode("OCODE");
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper, productRepository, orderLineRepository);
    }

    @Test
    void createOrder_CustomerNotFound_ShouldThrow() {
        when(orderRepository.existsByCode("OCODE")).thenReturn(false);
        when(customerRepository.findByCf("CF")).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> service.createOrder(baseDto));
    }

    @Test
    void createOrder_ProductNotFoundInLines_ShouldThrow() {
        OrderLineDto l1 = new OrderLineDto(0L, "P1", 2);
        OrderLineDto l2 = new OrderLineDto(0L, "P2", 1);

        Set<OrderLineDto> lines = new LinkedHashSet<>(Arrays.asList(l1, l2));
        OrderDto dto = new OrderDto(1L, "OCODE", State.OPEN, "CF", null, lines);

        when(orderRepository.existsByCode("OCODE")).thenReturn(false);
        when(customerRepository.findByCf("CF")).thenReturn(customer);
        when(productRepository.existsByNationalCode("P1")).thenReturn(true);
        when(productRepository.existsByNationalCode("P2")).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> service.createOrder(dto));

        verify(productRepository).existsByNationalCode("P1");
        verify(productRepository).existsByNationalCode("P2");
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void createOrder_Success_NoLines_ShouldSaveAndMap() {
        when(orderRepository.existsByCode("OCODE")).thenReturn(false);
        when(customerRepository.findByCf("CF")).thenReturn(customer);
        when(orderMapper.toEntity(baseDto)).thenReturn(mappedEntity);
        when(orderRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(orderMapper.toDto(savedEntity)).thenReturn(baseDto);

        OrderDto out = service.createOrder(baseDto);

        assertEquals(baseDto, out);
        verify(orderLineRepository, never()).saveAll(anyCollection());
    }

    @Test
    void createOrder_Success_WithLines_ShouldInvokeSaveAllOnSavedLines() {
        OrderLine lineA = new OrderLine();
        lineA.setId(10L);
        Order withLines = new Order();
        withLines.setId(1L);
        withLines.setCode("OCODE");
        withLines.setOrderLines(new HashSet<>(Set.of(lineA)));

        OrderDto expectedDto = new OrderDto(1L, "OCODE", State.OPEN, "CF", null, new HashSet<>());

        when(orderRepository.existsByCode("OCODE")).thenReturn(false);
        when(customerRepository.findByCf("CF")).thenReturn(customer);
        when(orderMapper.toEntity(baseDto)).thenReturn(mappedEntity);
        when(orderRepository.save(mappedEntity)).thenReturn(withLines);
        when(orderMapper.toDto(withLines)).thenReturn(expectedDto);

        OrderDto out = service.createOrder(baseDto);

        assertEquals(expectedDto, out);
        verify(orderLineRepository).saveAll(withLines.getOrderLines());
    }

    // ----------->GET ORDER BY ID<-----------

    @Test
    void getOrderById_NotFound_ShouldThrow() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.getOrderById(1L));
    }

    @Test
    void getOrderById_Found_ShouldMap() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedEntity));
        when(orderMapper.toDto(savedEntity)).thenReturn(baseDto);

        OrderDto out = service.getOrderById(1L);

        assertEquals(baseDto, out);
    }

    // ----------->GET ORDER BY CODE<-----------

    @Test
    void getOrderByCode_NotFound_ShouldThrow() {
        when(orderRepository.findByCode("OCODE")).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> service.getOrderByCode("OCODE"));
    }

    @Test
    void getOrderByCode_Found_ShouldMap() {
        when(orderRepository.findByCode("OCODE")).thenReturn(savedEntity);
        when(orderMapper.toDto(savedEntity)).thenReturn(baseDto);

        OrderDto out = service.getOrderByCode("OCODE");

        assertEquals(baseDto, out);
    }

    // ----------->GET ALL ORDERS<-----------

    @Test
    void getAllOrder_ShouldMapAll() {
        Order e1 = new Order(); e1.setId(1L);
        Order e2 = new Order(); e2.setId(2L);
        OrderDto d1 = new OrderDto(); d1.setId(1L);
        OrderDto d2 = new OrderDto(); d2.setId(2L);

        when(orderRepository.findAll()).thenReturn(List.of(e1, e2));
        when(orderMapper.toDto(e1)).thenReturn(d1);
        when(orderMapper.toDto(e2)).thenReturn(d2);

        List<OrderDto> out = service.getAllOrder();

        assertEquals(2, out.size());
        assertEquals(1L, out.get(0).getId());
        assertEquals(2L, out.get(1).getId());
    }

    // ----------->UPDATE STATE<-----------

    @Test
    void updateState_NotFound_ShouldThrow() {
    	when(orderRepository.findByCode("OCODE")).thenReturn(null);

    	assertThrows(OrderNotFoundException.class, () -> service.updateState("OCODE", State.SHIPPING));
    }

    @Test
    void updateState_InvalidTransitionCompletedToOpen_ShouldThrow() {
    	Order e = new Order();
    	e.setState(State.COMPLETED);
    	when(orderRepository.findByCode("OCODE")).thenReturn(e);

    	assertThrows(InvalidOrderOperationException.class, () -> service.updateState("OCODE", State.OPEN));
    }

    @Test
    void updateState_Valid_ShouldPersistAndMap() {
    	Order e = new Order();
    	e.setState(State.OPEN);
    	OrderDto mapped = new OrderDto();
    	when(orderRepository.findByCode("OCODE")).thenReturn(e);
    	when(orderRepository.save(e)).thenReturn(e);
    	when(orderMapper.toDto(e)).thenReturn(mapped);

    	OrderDto out = service.updateState("OCODE", State.SHIPPING);

    	assertEquals(mapped, out);
    	assertEquals(State.SHIPPING, e.getState());
    	verify(orderRepository).save(e);
    }

    // ----------->ADD LINE<-----------

    @Test
    void addLine_ProductNotFound_ShouldThrow() {
        OrderLineDto lineDto = new OrderLineDto(0L, "P1", 1);
        when(productRepository.existsByNationalCode("P1")).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> service.addLine("OCODE", lineDto));
    }

    @Test
    void addLine_OrderNotFound_ShouldThrow() {
        OrderLineDto lineDto = new OrderLineDto(0L, "P1", 1);
        when(productRepository.existsByNationalCode("P1")).thenReturn(true);
        when(orderRepository.findByCode("OCODE")).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> service.addLine("OCODE", lineDto));
    }

    @Test
    void addLine_NewLine_ShouldAddAndMap() {
        Order e = new Order();
        e.setOrderLines(new HashSet<>());

        OrderLineDto lineDto = new OrderLineDto(0L, "P1", 2);
        OrderLine lineEntity = new OrderLine();
        OrderDto mapped = new OrderDto();

        when(productRepository.existsByNationalCode("P1")).thenReturn(true);
        when(orderRepository.findByCode("OCODE")).thenReturn(e);
        when(orderLineMapper.toEntity(lineDto)).thenReturn(lineEntity);
        when(orderRepository.save(e)).thenReturn(e);
        when(orderMapper.toDto(e)).thenReturn(mapped);

        OrderDto out = service.addLine("OCODE", lineDto);

        assertEquals(mapped, out);
        assertTrue(e.getOrderLines().contains(lineEntity));
        verify(orderLineRepository).save(lineEntity);
        verify(orderRepository).save(e);
    }

    @Test
    void addLine_ExistingLine_ShouldIncreaseQuantityAndSaveExisting() {
        Order e = new Order();
        OrderLine existing = new OrderLine();
        existing.setNationalCode("P1");
        existing.setQuantity(5);
        e.setOrderLines(new HashSet<>(Set.of(existing)));

        OrderLineDto lineDto = new OrderLineDto(0L, "P1", 3);
        OrderDto mapped = new OrderDto();

        when(productRepository.existsByNationalCode("P1")).thenReturn(true);
        when(orderRepository.findByCode("OCODE")).thenReturn(e);
        when(orderRepository.save(e)).thenReturn(e);
        when(orderMapper.toDto(e)).thenReturn(mapped);

        OrderDto out = service.addLine("OCODE", lineDto);

        assertEquals(8, existing.getQuantity());
        assertEquals(mapped, out);
        verify(orderLineRepository).save(existing);
        verify(orderRepository).save(e);
        verify(orderLineMapper, never()).toEntity(any());
    }

    // ----------->UPDATE LINE QUANTITY<-----------

    @Test
    void updateLineQuantity_NotFound_ShouldThrow() {
        when(orderLineRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(OrderLineNotFoundException.class, () -> service.updateLineQuantity(77L, 11));
    }

    @Test
    void updateLineQuantity_Found_ShouldPersistAndReturnOrderDto() {
        Order orderRef = new Order();
        orderRef.setId(123L);

        OrderLine line = new OrderLine();
        line.setId(77L);
        line.setQuantity(2);
        line.setOrder(orderRef);

        OrderDto mapped = new OrderDto();

        when(orderLineRepository.findById(77L)).thenReturn(Optional.of(line));
        when(orderLineRepository.save(line)).thenReturn(line);
        when(orderRepository.findById(123L)).thenReturn(Optional.of(orderRef));
        when(orderMapper.toDto(orderRef)).thenReturn(mapped);

        OrderDto out = service.updateLineQuantity(77L, 11);

        assertEquals(11, line.getQuantity());
        assertEquals(mapped, out);
        verify(orderLineRepository).save(line);
        verify(orderRepository).findById(123L);
    }

    // ----------->REMOVE LINE<-----------

    @Test
    void removeLine_NotFound_ShouldThrow() {
        when(orderLineRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(OrderLineNotFoundException.class, () -> service.removeLine(7L));
    }

    @Test
    void removeLine_NoOrder_ShouldThrow() {
        OrderLine line = new OrderLine();
        line.setId(7L);
        line.setOrder(null);

        when(orderLineRepository.findById(7L)).thenReturn(Optional.of(line));

        assertThrows(InvalidOrderOperationException.class, () -> service.removeLine(7L));
    }

    @Test
    void removeLine_WithOrder_ShouldDeleteLineAndSaveOrder() {
        Order order = new Order();
        OrderLine line = new OrderLine();
        line.setId(7L);
        line.setOrder(order);
        order.setOrderLines(new HashSet<>(Set.of(line)));

        OrderDto mapped = new OrderDto();

        when(orderLineRepository.findById(7L)).thenReturn(Optional.of(line));
        doNothing().when(orderLineRepository).delete(line);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(mapped);

        OrderDto out = service.removeLine(7L);

        assertEquals(mapped, out);
        assertTrue(order.getOrderLines().isEmpty());
        verify(orderLineRepository).delete(line);
        verify(orderRepository).save(order);
    }

    // ----------->CLEAR LINES<-----------

    @Test
    void clearLines_OrderNotFound_ShouldThrow() {
        when(orderRepository.findByCode("OCODE")).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> service.clearLines("OCODE"));
    }

    @Test
    void clearLines_WithLines_ShouldClearAndMap() {
        Order e = new Order();

        OrderLine l1 = new OrderLine();
        l1.setId(1L);
        l1.setNationalCode("A");
        l1.setQuantity(1);

        OrderLine l2 = new OrderLine();
        l2.setId(2L);
        l2.setNationalCode("B");
        l2.setQuantity(2);

        e.setOrderLines(new HashSet<>(Set.of(l1, l2)));

        OrderDto mapped = new OrderDto();

        when(orderRepository.findByCode("OCODE")).thenReturn(e);
        when(orderRepository.save(e)).thenReturn(e);
        when(orderMapper.toDto(e)).thenReturn(mapped);

        OrderDto out = service.clearLines("OCODE");

        assertEquals(mapped, out);
        assertTrue(e.getOrderLines().isEmpty());
        verify(orderRepository).save(e);
    }

    // ----------->DELETE ORDER<-----------

    @Test
    void deleteOrder_NotFound_ShouldThrow() {
        when(orderRepository.findByCode("OCODE")).thenReturn(null);

        assertThrows(OrderNotFoundException.class, () -> service.deleteOrder("OCODE"));
    }

    @Test
    void deleteOrder_Found_ShouldDeleteAndReturnDto() {
        Order e = new Order();
        OrderDto mapped = new OrderDto();

        when(orderRepository.findByCode("OCODE")).thenReturn(e);
        doNothing().when(orderRepository).delete(e);
        when(orderMapper.toDto(e)).thenReturn(mapped);

        OrderDto out = service.deleteOrder("OCODE");

        assertEquals(mapped, out);
        verify(orderRepository).delete(e);
    }
}
