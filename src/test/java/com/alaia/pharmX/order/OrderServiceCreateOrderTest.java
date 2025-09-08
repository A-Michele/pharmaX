package com.alaia.pharmX.order;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.stock.StockService;
import com.alaia.pharmX.servicesImpl.order.OrderServiceImp;
import org.mockito.Mock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.repositories.order.OrderLineRepository;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("com.alaia.pharmX.models")
@EnableJpaRepositories(basePackages = "com.alaia.pharmX.repositories")
class OrderServiceCreateOrderTest {

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
    	availableQuantity = new AvailableQuantityProduct();
        orderLineMapper = new OrderLineMapper();
        orderMapper = new OrderMapper(orderLineMapper);
        orderService = new OrderServiceImp(productRepository, stockService, customerRepository, orderMapper, orderLineMapper, orderRepository, orderLineRepository);
    }

    @Test
    void createOrder_Success() {
        // Arrange
    	Set<OrderLineDto> lines= new HashSet<>();
    	OrderDto orderRequest = new OrderDto(0L, null , State.OPEN, "CF123" , LocalDateTime.now(), lines);
    	OrderLineDto orderLineRequest = new OrderLineDto(0L, "ABC123", 5, null, LineOrderType.OPEN);
    	orderRequest.getOrderLines().add(orderLineRequest);

    	availableQuantity.setNationalCode("ABC123");
    	availableQuantity.setAvailableQuantity(10);

    	when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        // Act
        OrderDto result = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCode().startsWith("ORD-CF-"), "Order code should start with ORD-CF-");
        assertEquals("CF123", result.getCf());

        verify(stockService).getAvailableQuantity("ABC123");
        verify(stockService).reserveQuantity(argThat(op ->
                op.getNationalCode().equals("ABC123") &&
                op.getReferenceType().equals("ORDER") &&
                op.getReferenceId() > 0 &&
                op.getType() == MovementType.ORDER_ALLOCATION &&
                op.getQuantity() == 5
        ));
        verifyNoMoreInteractions(stockService);
    }

    @Test
    void createOrder_CustomerNotFound_ThrowsException() {
        // Arrange
    	String orderCode = "ORD-CF-INVALID-CF";
    	OrderDto request = orderService.getOrderByCode(orderCode);

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () ->
                orderService.createOrder(request));
        verify(stockService, never()).getAvailableQuantity(any());
    }

    @Test
    void createOrder_ProductNotFound_ThrowsException() {
    	String orderCode ="ORD-NC-INVALID";
    	OrderDto request = orderService.getOrderByCode(orderCode);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                orderService.createOrder(request));
        verifyNoMoreInteractions(stockService);
    }

    @Test
    void createOrder_StockNotAvailable_ThrowsException() {
        // Arrange
    	String orderCode ="ORD-CF-123";
    	OrderDto request = orderService.getOrderByCode(orderCode);

    	availableQuantity.setNationalCode("ABC123");
    	availableQuantity.setAvailableQuantity(10);

    	when(stockService.getAvailableQuantity("ABC123")).thenReturn(null);

        // Act & Assert
        assertThrows(StockNotAvailableException.class, () ->
                orderService.createOrder(request));

    }
}
