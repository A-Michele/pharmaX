//package com.alaia.pharmX.orderLine;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import com.alaia.pharmX.dtos.OrderLineDto;
//import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
//import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
//import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
//import com.alaia.pharmX.mappers.OrderLineMapper;
//import com.alaia.pharmX.models.Order;
//import com.alaia.pharmX.models.OrderLine;
//import com.alaia.pharmX.repositories.OrderLineRepository;
//import com.alaia.pharmX.repositories.OrderRepository;
//import com.alaia.pharmX.repositories.ProductRepository;
//import com.alaia.pharmX.servicesImpl.OrderLineServiceImp;
//
//@ExtendWith(MockitoExtension.class)
//public class OrderLineServiceTest {
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private OrderLineMapper orderLineMapper;
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private OrderLineRepository orderLineRepository;
//
//    @InjectMocks
//    private OrderLineServiceImp service;
//
//    private Order order;
//    private OrderLine existingLine;
//    private OrderLine newLineEntity;
//    private OrderLineDto newLineDto;
//    private OrderLineDto mappedDto;
//
////    @BeforeEach
////    void setUp() {
////        order = new Order();
////        order.setId(100L);
////
////        existingLine = new OrderLine();
////        existingLine.setId(10L);
////        existingLine.setNationalCode("NCODE");
////        existingLine.setQuantity(2);
////        existingLine.setOrder(order);
////
////        newLineEntity = new OrderLine();
////        newLineEntity.setId(20L);
////        newLineEntity.setNationalCode("NCODE2");
////        newLineEntity.setQuantity(3);
////        newLineEntity.setOrder(order);
////
////        newLineDto = new OrderLineDto(0L, "NCODE2", 3);
////
////        mappedDto = new OrderLineDto(99L, "NCODE2", 3);
////    }
//
//    // ----------->CREATE FOR ORDER<-----------
//
//    @Test
//    void createForOrder_ProductNotFound_ShouldThrow() {
//        when(productRepository.existsByNationalCode("NCODE2")).thenReturn(false);
//
//        assertThrows(ProductNotFoundException.class,
//                () -> service.createForOrder("OCODE", newLineDto));
//
//        verify(productRepository).existsByNationalCode("NCODE2");
//        verifyNoInteractions(orderRepository, orderLineRepository, orderLineMapper);
//    }
//
//    @Test
//    void createForOrder_OrderNotFound_ShouldThrow() {
//        when(productRepository.existsByNationalCode("NCODE2")).thenReturn(true);
//        when(orderRepository.findByCode("OCODE")).thenReturn(null);
//
//        assertThrows(OrderNotFoundException.class,
//                () -> service.createForOrder("OCODE", newLineDto));
//
//        verify(orderRepository).findByCode("OCODE");
//    }
//
////    @Test
////    void createForOrder_ExistingLine_ShouldIncreaseQuantityAndReturnMappedDto() {
////        order.setOrderLines(new HashSet<>(Set.of(existingLine)));
////
////        OrderLineDto expected = new OrderLineDto(existingLine.getId(), existingLine.getNationalCode(), 5);
////
////        when(productRepository.existsByNationalCode("NCODE")).thenReturn(true);
////        when(orderRepository.findByCode("OCODE")).thenReturn(order);
////        when(orderLineRepository.save(existingLine)).thenReturn(existingLine);
////        when(orderLineMapper.toDto(existingLine)).thenReturn(expected);
////
////        OrderLineDto result = service.createForOrder("OCODE", new OrderLineDto(0L, "NCODE", 3));
////
////        assertEquals(5, existingLine.getQuantity());
////        assertEquals(expected, result);
////        verify(orderLineRepository).save(existingLine);
////        verify(orderLineMapper).toDto(existingLine);
////    }
//
//    @Test
//    void createForOrder_NewLine_OrderAlreadyHasSet_ShouldAddAndReturnMappedDto() {
//        order.setOrderLines(new HashSet<>());
//
//        when(productRepository.existsByNationalCode("NCODE2")).thenReturn(true);
//        when(orderRepository.findByCode("OCODE")).thenReturn(order);
//        when(orderLineMapper.toEntity(newLineDto)).thenReturn(newLineEntity);
//        when(orderLineRepository.save(newLineEntity)).thenReturn(newLineEntity);
//        when(orderLineMapper.toDto(newLineEntity)).thenReturn(mappedDto);
//
//        OrderLineDto result = service.createForOrder("OCODE", newLineDto);
//
//        assertTrue(order.getOrderLines().contains(newLineEntity));
//        assertEquals(mappedDto, result);
//        verify(orderLineRepository).save(newLineEntity);
//    }
//
//    @Test
//    void createForOrder_NewLine_OrderLinesNull_ShouldInitSetAndAdd() {
//        order.setOrderLines(null);
//
//        when(productRepository.existsByNationalCode("NCODE2")).thenReturn(true);
//        when(orderRepository.findByCode("OCODE")).thenReturn(order);
//        when(orderLineMapper.toEntity(newLineDto)).thenReturn(newLineEntity);
//        when(orderLineRepository.save(newLineEntity)).thenReturn(newLineEntity);
//        when(orderLineMapper.toDto(newLineEntity)).thenReturn(mappedDto);
//
//        OrderLineDto result = service.createForOrder("OCODE", newLineDto);
//
//        assertNotNull(order.getOrderLines());
//        assertTrue(order.getOrderLines().contains(newLineEntity));
//        assertEquals(mappedDto, result);
//    }
//
//    // ----------->GET BY ID<-----------
//
//    @Test
//    void getById_Found_ShouldReturnDto() {
//        when(orderLineRepository.findById(1L)).thenReturn(Optional.of(existingLine));
//        when(orderLineMapper.toDto(existingLine)).thenReturn(mappedDto);
//
//        OrderLineDto result = service.getById(1L);
//
//        assertEquals(mappedDto, result);
//        verify(orderLineRepository).findById(1L);
//        verify(orderLineMapper).toDto(existingLine);
//    }
//
//    @Test
//    void getById_NotFound_ShouldThrow() {
//        when(orderLineRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThrows(OrderLineNotFoundException.class, () -> service.getById(1L));
//    }
//
//    // ----------->GET BY ORDER CODE<-----------
//
////    @Test
////    void getByOrderCode_ShouldMapAll() {
////        when(orderLineRepository.findByOrder_Code("OCODE")).thenReturn(List.of(existingLine, newLineEntity));
////        OrderLineDto dto1 = new OrderLineDto(10L, "NCODE", 2);
////        OrderLineDto dto2 = new OrderLineDto(20L, "NCODE2", 3);
////        when(orderLineMapper.toDto(existingLine)).thenReturn(dto1);
////        when(orderLineMapper.toDto(newLineEntity)).thenReturn(dto2);
////
////        List<OrderLineDto> result = service.getByOrderCode("OCODE");
////
////        assertEquals(2, result.size());
////        assertEquals(dto1, result.get(0));
////        assertEquals(dto2, result.get(1));
////    }
//
//    // ----------->UPDATE QUANTITY<-----------
//
////    @Test
////    void updateQuantity_Found_ShouldPersistAndReturnDto() {
////        when(orderLineRepository.findById(10L)).thenReturn(Optional.of(existingLine));
////        when(orderLineRepository.save(existingLine)).thenReturn(existingLine);
////        when(orderLineMapper.toDto(existingLine)).thenReturn(mappedDto);
////
////        OrderLineDto result = service.updateQuantity(10L, 42);
////
////        assertEquals(42, existingLine.getQuantity());
////        assertEquals(mappedDto, result);
////        verify(orderLineRepository).save(existingLine);
////    }
//
////    @Test
////    void updateQuantity_NotFound_ShouldThrow() {
////        when(orderLineRepository.findById(10L)).thenReturn(Optional.empty());
////
////        assertThrows(OrderLineNotFoundException.class, () -> service.updateQuantity(10L, 1));
////    }
//
//    // ----------->DELETE<-----------
//
////    @Test
////    void delete_WithOrderAndSet_ShouldRemoveDeleteAndReturnDto() {
////        order.setOrderLines(new HashSet<>(Set.of(existingLine)));
////
////        when(orderLineRepository.findById(10L)).thenReturn(Optional.of(existingLine));
////        when(orderLineMapper.toDto(existingLine)).thenReturn(mappedDto);
////
////        OrderLineDto result = service.delete(10L);
////
////        assertFalse(order.getOrderLines().contains(existingLine));
////        verify(orderLineRepository).delete(existingLine);
////        assertEquals(mappedDto, result);
////    }
//
//    @Test
//    void delete_WithOrderButSetNull_ShouldJustDeleteAndReturnDto() {
//        order.setOrderLines(null);
//        existingLine.setOrder(order);
//
//        when(orderLineRepository.findById(10L)).thenReturn(Optional.of(existingLine));
//        when(orderLineMapper.toDto(existingLine)).thenReturn(mappedDto);
//
//        OrderLineDto result = service.delete(10L);
//
//        verify(orderLineRepository).delete(existingLine);
//        assertEquals(mappedDto, result);
//    }
//
////    @Test
////    void delete_OrderNull_ShouldJustDeleteAndReturnDto() {
////        OrderLine line = new OrderLine();
////        line.setId(33L);
////        line.setOrder(null);
////
////        when(orderLineRepository.findById(33L)).thenReturn(Optional.of(line));
////        when(orderLineMapper.toDto(line)).thenReturn(new OrderLineDto(33L, "X", 1));
////
////        OrderLineDto result = service.delete(33L);
////
////        verify(orderLineRepository).delete(line);
////        assertNotNull(result);
////    }
//}