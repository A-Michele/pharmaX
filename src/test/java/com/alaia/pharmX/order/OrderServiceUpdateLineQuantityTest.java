package com.alaia.pharmX.order;

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
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderLineServiceImp;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("com.alaia.pharmX.models")
@EnableJpaRepositories(basePackages = "com.alaia.pharmX.repositories")
class OrderServiceUpdateLineQuantityTest {

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
    	availableQuantity = new AvailableQuantityProduct();
        orderLineMapper = new OrderLineMapper();
        orderMapper = new OrderMapper(orderLineMapper);
        orderService = new OrderServiceImp(productRepository, stockService, customerRepository, orderMapper, orderLineMapper, orderRepository, orderLineRepository);
        orderLineService= new OrderLineServiceImp(productRepository, orderLineMapper, orderRepository, orderLineRepository, stockService);
    }

    @Test
    void updateLineQuantity_IncreaseQuantity_Success() {
        // Arrange
    	String lineNumber = "ORDLINE-123456";
		OrderLineDto request = orderLineService.getByLineNumber(lineNumber);

	    availableQuantity.setNationalCode("ABC123");
	    availableQuantity.setAvailableQuantity(10);
	    when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        int newQuantity = 7;

        // Act
        OrderDto result = orderService.updateLineQuantity(request.getId(), newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(7,result.getOrderLines().iterator().next().getQuantity());
        verify(stockService).reserveQuantity(argThat(op ->
            op.getNationalCode().equals("ABC123") &&
            op.getReferenceType().equals("ORDER") &&
            op.getReferenceId() == 1L &&
            op.getType() == MovementType.ADJUSTMENT &&
            op.getQuantity() == 2
        ));
    }

    @Test
    void updateLineQuantity_DecreaseQuantity_Success() {
    	//Arrange
    	String lineNumber = "ORDLINE-123456";
    	OrderLineDto request = orderLineService.getByLineNumber(lineNumber);

    	availableQuantity.setNationalCode("ABC123");
    	availableQuantity.setAvailableQuantity(10);
    	when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

    	int newQuantity = 2;

    	//Act
    	OrderDto result = orderService.updateLineQuantity(request.getId(), newQuantity);

    	//Assert
    	assertNotNull(result);
    	assertEquals(2,result.getOrderLines().iterator().next().getQuantity());
    	verify(stockService).unReserveQuantityOnDeleteOrCanceled(argThat(op ->
    	op.getNationalCode().equals("ABC123") &&
    	op.getReferenceType().equals("ORDER") &&
    	op.getReferenceId() == 1L &&
    	op.getType() == MovementType.ADJUSTMENT &&
    	op.getQuantity() == 3
        ));
    }

    @Test
    void updateLineQuantity_OrderLineNotFound_ThrowsException() {
        // Arrange
    	long orderLineId = 9999L;
    	int newQuantity = 7;

        // Act & Assert
        assertThrows(OrderLineNotFoundException.class, () ->
                orderService.updateLineQuantity(orderLineId, newQuantity));
        Optional<OrderLine> line = orderLineRepository.findById(orderLineId);
        assertTrue(line.isEmpty());
    }

    @Test
    void updateLineQuantity_OrderNotOpen_ThrowsException() {
    	// Arrange
    	String lineNumber = "ORDLINE-ORD-NO-OPEN";
    	OrderLineDto request = orderLineService.getByLineNumber(lineNumber);
    	int newQuantity = 7;

    	// Act & Assert
    	assertThrows(InvalidUpdateQuantityException.class, () ->
    	orderService.updateLineQuantity(request.getId(), newQuantity));
    	OrderLine line = orderLineRepository.findByLineNumber(lineNumber);
    	assertEquals(5,line.getQuantity());
    	verify(stockService, never()).reserveQuantity(any());
    	verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
    }

    @Test
    void updateLineQuantity_QuantityNotAvailable_ThrowsException() {
        // Arrange
    	String lineNumber = "ORDLINE-123456";
    	OrderLineDto request = orderLineService.getByLineNumber(lineNumber);

    	availableQuantity.setNationalCode("ABC123");
    	availableQuantity.setAvailableQuantity(10);
    	when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        int newQuantity = 15;

        // Act & Assert
        assertThrows(QuantityNotAvailableException.class, () ->
                orderService.updateLineQuantity(request.getId(), newQuantity));
        verify(stockService, never()).reserveQuantity(any());
    	verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());
    	OrderLine line = orderLineRepository.findByLineNumber(lineNumber);
    	assertEquals(5,line.getQuantity());

    }

    @Test
    void updateLineQuantity_SameQuantity_ThrowsException() {
        // Arrange
    	String lineNumber = "ORDLINE-123456";
    	OrderLineDto request = orderLineService.getByLineNumber(lineNumber);

    	availableQuantity.setNationalCode("ABC123");
    	availableQuantity.setAvailableQuantity(10);
    	when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

    	int newQuantity = 5;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateLineQuantity(request.getId(), newQuantity));
        verify(stockService, never()).reserveQuantity(any());
        verify(stockService, never()).unReserveQuantityOnDeleteOrCanceled(any());

    }
}
