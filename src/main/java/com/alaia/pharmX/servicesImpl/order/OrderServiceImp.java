package com.alaia.pharmX.servicesImpl.order;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import com.alaia.pharmX.dtos.order.FilterOrdersToRelease;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteOrderWithNoStateOpenException;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.order.OrderService;
import com.alaia.pharmX.services.stock.StockService;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class OrderServiceImp implements OrderService{

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private StockService stockService;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
    private OrderMapper orderMapper;

	@Autowired
    private OrderLineMapper orderLineMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderLineRepository orderLineRepository;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto createOrder(OrderDto orderDto) {
		// Initial validation
		validateOrder(orderDto);

		// Saving order
		Order savedOrder = saveOrder(orderDto);

		// Product quantity management
		reserveProductQuantities(orderDto, savedOrder.getId());
		return orderMapper.toDto(savedOrder);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto getOrderById(long id) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

		return orderMapper.toDto(order);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto getOrderByCode(String code) {

		Order order = orderRepository.findByCode(code);

		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + code);
		}

		return orderMapper.toDto(order);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<OrderDto> getAllOrder() {

		return orderRepository.findAll().stream()
				.map(orderMapper::toDto)
				.toList();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto addLine(String orderCode, OrderLineDto lineDto) {
		// Order and product validation
		validateOrderAndProduct(orderCode, lineDto);

		// Product quantity reserve operations
		reserveProductQuantity(lineDto, orderCode);

		// Add or update the order line
		updateOrderLine(orderCode, lineDto);

		// Save and return the order DTO
		return orderMapper.toDto(orderRepository.save(orderRepository.findByCode(orderCode)));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto updateLineQuantity(long orderLineId, int newQuantity) {
		// Line and order validation
		OrderLine line = validateLineAndOrder(orderLineId, newQuantity);

		// Quantity gap calculation
		int gap = calculateGap(line, newQuantity);

		// Reserve management or cancellation of the reserve
		manageStockOperation(line, newQuantity, gap);

		// Update quantity
		line.setQuantity(newQuantity);

		// Saving the updated line
		orderLineRepository.save(line);

		// Return the order DTO
		return orderMapper.toDto(orderRepository.findById(line.getOrder().getId())
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + line.getOrder().getId())));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto removeLine(long orderLineId) {
		// Line and order validation
		OrderLine line = validateLineAndOrder(orderLineId);

		// Handling order line removal
		removeOrderLine(line);

		// Saving the updated order
		return saveUpdatedOrder(line.getOrder());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto clearLines(String orderCode) {
		// Order validation
		Order order = validateOrderForClear(orderCode);

		// Handling line removal
		clearOrderLines(order);

		// Saving the updated order
		return saveOrder(order);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderDto deleteOrder(String code) {
		// Order validation
		Order order = validateOrder(code);

		// Handling order line removal
		manageOrderLines(order);

		// Updating the status and saving the order
		return saveOrderWithCanceledState(order);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<OrderDto> getOrdersByFilter(FilterOrdersToRelease filterOrders){
		if (filterOrders == null) {
	        throw new IllegalArgumentException("The filter cannot be empty");
	    }

		verifiedCf(filterOrders.getCf());

		List<Order> orders = orderRepository.findOrdersByCfOrDateRange(filterOrders);
	    if (orders == null) {
	        return Collections.emptyList();
	    }

	    return orders.stream()
				.map(orderMapper::toDto)
				.toList();
	}

	//------> HELEPERS FOR CREATE ORDER <-------

	private void validateOrder(OrderDto orderDto) {

	    Customer customer = customerRepository.findByCf(orderDto.getCf());
	    if (customer == null) {
	        throw new CustomerNotFoundException("Customer not found with CF : " + orderDto.getCf());
	    }

	    for (OrderLineDto l : orderDto.getOrderLines()) {
	        if (!productRepository.existsByNationalCode(l.getNationalCode())) {
	            throw new ProductNotFoundException("Product not found with nationalCode: " + l.getNationalCode());
	        }

	        AvailableQuantityProduct aqP = stockService.getAvailableQuantity(l.getNationalCode());
	        if(aqP == null) {
	            throw new StockNotAvailableException("Stock not available for product: " + l.getNationalCode());
	        }
	    }
	}

	private Order saveOrder(OrderDto orderDto) {
		LocalDateTime now = LocalDateTime.now();

		orderDto.setCode(generateOrderCode(orderDto.getCf()));

		if (orderRepository.existsByCode(orderDto.getCode())) {
	        throw new OrderAlreadyExistsException("Order already exists with code: " + orderDto.getCode());
	    }

		orderDto.setDate(now);
		orderDto.setState(State.OPEN);
	    Order entity = orderMapper.toEntity(orderDto);
	    return orderRepository.saveAndFlush(entity);
	}

	private String generateOrderCode(String cf) {
	    String prefix = cf.substring(0, 2).toUpperCase();
	    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	    StringBuilder rand = new StringBuilder();
	    for (int i = 0; i < 3; i++) {
	        rand.append(chars.charAt((int) (Math.random() * chars.length())));
	    }
	    return "ORD-" + prefix + "-" + rand;
	}

	private void reserveProductQuantities(OrderDto orderDto, Long orderId) {
	    for (OrderLineDto l : orderDto.getOrderLines()) {
	        StockOperation ope = buildStockOperation(l, MovementType.ORDER_ALLOCATION, l.getQuantity(), orderId);
	        stockService.reserveQuantity(ope);
	    }
	}

	//------> HELEPERS FOR ADD LINE TO ORDER <-------

	private void validateOrderAndProduct(String orderCode, OrderLineDto lineDto) {

	    if (!productRepository.existsByNationalCode(lineDto.getNationalCode())) {
	        throw new ProductNotFoundException("Product not found with nationalCode: " + lineDto.getNationalCode());
	    }

	    Order order = orderRepository.findByCode(orderCode);
	    if (order == null) {
	        throw new OrderNotFoundException("Order not found with code: " + orderCode);
	    }

	    if (order.getState() != State.OPEN) {
	        throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN. Current order state: " + order.getState());
	    }

	    if (order.getOrderLines() == null) {
	        order.setOrderLines(new HashSet<>());
	    }
	}

	private void reserveProductQuantity(OrderLineDto lineDto, String orderCode) {
	    Order order = orderRepository.findByCode(orderCode);
	    StockOperation ope = buildStockOperation(lineDto, MovementType.ORDER_ALLOCATION, lineDto.getQuantity(), order.getId());
	    stockService.reserveQuantity(ope);
	}

	private void updateOrderLine(String orderCode, OrderLineDto lineDto) {
	    Order order = orderRepository.findByCode(orderCode);

	    OrderLine existing = order.getOrderLines().stream()
	            .filter(l -> lineDto.getNationalCode().equals(l.getNationalCode()))
	            .findFirst()
	            .orElse(null);

	    if (existing != null) {
	        existing.setQuantity(existing.getQuantity() + lineDto.getQuantity());
	        orderLineRepository.save(existing);
	    } else {
	        lineDto.setType(LineOrderType.OPEN);
	        String rand = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
	        lineDto.setLineNumber("ORDLINE-" + rand);
	        OrderLine line = orderLineMapper.toEntity(lineDto);
	        line.setOrder(order);
	        order.getOrderLines().add(line);
	        orderLineRepository.save(line);
	    }
	}

	//------> HELEPERS FOR UPDATE ORDER LINE QUANTITY <-------

	private OrderLine validateLineAndOrder(long orderLineId, int newQuantity) {
	    OrderLine line = orderLineRepository.findById(orderLineId)
	            .orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

	    if (line.getOrder().getState() != State.OPEN) {
	        throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN. Current order state: " + line.getOrder().getState());
	    }

	    AvailableQuantityProduct aqP = stockService.getAvailableQuantity(line.getNationalCode());
	    if (aqP == null) {
	        throw new StockNotAvailableException("Stock not available for product: " + line.getNationalCode());
	    }

	    if (newQuantity > aqP.getAvailableQuantity()) {
	        throw new QuantityNotAvailableException("Requested quantity " + newQuantity + " exceeds available stock of " + aqP.getAvailableQuantity());
	    }

	    return line;
	}

	private int calculateGap(OrderLine line, int newQuantity) {
	    int gap = 0;
	    if (newQuantity > line.getQuantity()) {
	        gap = newQuantity - line.getQuantity();
	        AvailableQuantityProduct aqP = stockService.getAvailableQuantity(line.getNationalCode());
	        if (gap > aqP.getAvailableQuantity()) {
	            throw new QuantityNotAvailableException("Quantity not available. Current quantity of line: " + line.getQuantity() + ", "
	                    + " requested more: " + gap + ", available in stock: " + aqP.getAvailableQuantity());
	        }
	    } else if (newQuantity < line.getQuantity()) {
	        gap = line.getQuantity() - newQuantity;
	    } else {
	        throw new IllegalArgumentException("Error: Current quantity equals new quantity.");
	    }
	    return gap;
	}

	private void manageStockOperation(OrderLine line, int newQuantity, int gap) {

	    StockOperation ope = buildStockOperation(orderLineMapper.toDto(line), MovementType.ADJUSTMENT, gap, line.getOrder().getId());

	    if (newQuantity < line.getQuantity()) {

	        stockService.unReserveQuantityOnDeleteOrCanceled(ope);
	    } else {

	        stockService.reserveQuantity(ope);
	    }
	}

	//------> HELEPERS FOR REMOVE ORDER LINE TO ORDER <-------

	private OrderLine validateLineAndOrder(long orderLineId) {

	    OrderLine line = orderLineRepository.findById(orderLineId)
	            .orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

	    Order order = line.getOrder();
	    if (order == null) {
	        throw new InvalidOrderOperationException("The row is not associated with any order");
	    }

	    if (order.getState() != State.OPEN) {
	        throw new InvalidOrderOperationException("Can only update if the order is in status: OPEN. Current order state: " + order.getState());
	    }

	    return line;
	}

	private void removeOrderLine(OrderLine line) {
	    Order order = line.getOrder();

	    if (order.getOrderLines() != null) {
	        order.getOrderLines().removeIf(l -> l.getId() == line.getId());

	        StockOperation ope = buildStockOperation(orderLineMapper.toDto(line), MovementType.ADJUSTMENT, line.getQuantity(), order.getId());
	        stockService.unReserveQuantityOnDeleteOrCanceled(ope);
	    }

	    orderLineRepository.delete(line);
	}

	private OrderDto saveUpdatedOrder(Order order) {
	    Order savedOrder = orderRepository.save(order);
	    return orderMapper.toDto(savedOrder);
	}

	//------> HELEPERS FOR CLEAR ORDER LINE TO ORDER <-------

	private Order validateOrderForClear(String orderCode) {
	    Order order = orderRepository.findByCode(orderCode);
	    if (order == null) {
	        throw new OrderNotFoundException("Order not found with code: " + orderCode);
	    }

	    if (order.getState() != State.OPEN) {
	        throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN. Current order state: " + order.getState());
	    }

	    if (order.getOrderLines().isEmpty()) {
	        throw new InvalidOrderOperationException("Order: " + orderCode + " has nothing to clean. It has no order lines.");
	    }

	    return order;
	}

	private void clearOrderLines(Order order) {

	    for (OrderLine ol : order.getOrderLines()) {
	        StockOperation ope = buildStockOperation(orderLineMapper.toDto(ol), MovementType.ADJUSTMENT, ol.getQuantity(), order.getId());
	        stockService.unReserveQuantityOnDeleteOrCanceled(ope);
	    }

	    order.getOrderLines().clear();
	}

	private OrderDto saveOrder(Order order) {
	    Order savedOrder = orderRepository.save(order);
	    return orderMapper.toDto(savedOrder);
	}

	//------> HELEPERS FOR DELETE ORDER <-------

	private Order validateOrder(String code) {
	    Order order = orderRepository.findByCode(code);
	    if (order == null) {
	        throw new OrderNotFoundException("Order not found with code: " + code);
	    }

	    State state = order.getState();
	    if (state != State.OPEN) {
	        throw new CannotDeleteOrderWithNoStateOpenException("Cannot delete order with code: " + code +
	                ". Current state: " + state);
	    }

	    return order;
	}

	private void manageOrderLines(Order order) {
	    if (!order.getOrderLines().isEmpty()) {
	        for (OrderLine ol : order.getOrderLines()) {
	            StockOperation ope = buildStockOperation(orderLineMapper.toDto(ol), MovementType.RETURN, ol.getQuantity(), order.getId());
	            stockService.unReserveQuantityOnDeleteOrCanceled(ope);
	        }
	    }
	}

	private OrderDto saveOrderWithCanceledState(Order order) {
	    order.setState(State.CANCELED);
	    for(OrderLine line : order.getOrderLines())
	    	line.setType(LineOrderType.CANCELED);
	    Order savedOrder = orderRepository.save(order);
	    return orderMapper.toDto(savedOrder);
	}

	// -----> HELPERS GET ALL ORDER BY FILTER <-----

	private Customer verifiedCf(String cf) {
		Customer customer = customerRepository.findByCf(cf);
		if(customer == null ) throw new CustomerNotFoundException("Customer: " + cf + " not found ");
		return customer;
	}

	//------> HELEPERS<-------

	private StockOperation buildStockOperation(OrderLineDto dto, MovementType type, Integer quantity, Long referenceId) {
		StockOperation ope = new StockOperation();
		ope.setNationalCode(dto.getNationalCode());
		ope.setReferenceType("ORDER");
		ope.setReferenceId(referenceId);
		ope.setType(type);
		ope.setQuantity(quantity);

		return ope;
	}

}