package com.alaia.pharmX.servicesImpl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.OrderDto;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.dtos.receiving.StockItemDto;
import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteOrderWithOpenOrdersException;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidStateTransitionException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.OrderLineMapper;
import com.alaia.pharmX.mappers.OrderMapper;
import com.alaia.pharmX.models.Customer;
import com.alaia.pharmX.models.Order;
import com.alaia.pharmX.models.OrderLine;
import com.alaia.pharmX.models.State;
import com.alaia.pharmX.models.receiving.InventoryMovement;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.CustomerRepository;
import com.alaia.pharmX.repositories.OrderLineRepository;
import com.alaia.pharmX.repositories.OrderRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.receiving.InventoryMovementRepository;
import com.alaia.pharmX.services.OrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImp implements OrderService{

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private InventoryMovementRepository inventoryRepository;

	@Autowired
    private OrderMapper orderMapper;

	@Autowired
    private OrderLineMapper orderLineMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderLineRepository orderLineRepository;

	@Override
	@Transactional
	public OrderDto createOrder(OrderDto orderDto) {

		if (orderRepository.existsByCode(orderDto.getCode())) {
			throw new OrderAlreadyExistsException("Order already exists with code: " + orderDto.getCode());
		}

		Customer customer = customerRepository.findByCf(orderDto.getCf());
		if (customer == null) {
			throw new CustomerNotFoundException("Customer not found with CF : " + orderDto.getCf());
		}

		if (orderDto.getOrderLines() == null || orderDto.getOrderLines().isEmpty()) {
	        throw new IllegalArgumentException("Order must contain at least one line");
	    }

		if (orderDto.getOrderLines() != null) {
			for (OrderLineDto l : orderDto.getOrderLines()) {
				if (!productRepository.existsByNationalCode(l.getNationalCode())) {
					throw new ProductNotFoundException("Product not found with nationalCode: " + l.getNationalCode());
				}
			}
		}

		Order entity = orderMapper.toEntity(orderDto);
		if (entity.getOrderLines() == null) {
			entity.setOrderLines(new HashSet<>());
		}

		Order saved = orderRepository.save(entity);

		if (!saved.getOrderLines().isEmpty()) {
			orderLineRepository.saveAll(saved.getOrderLines());
		}

		return orderMapper.toDto(saved);
	}

	@Override
	@Transactional
	public OrderDto createOrderSafety(OrderDto orderDto) {

		if (orderRepository.existsByCode(orderDto.getCode())) {
			throw new OrderAlreadyExistsException("Order already exists with code: " + orderDto.getCode());
		}

		Customer customer = customerRepository.findByCf(orderDto.getCf());
		if (customer == null) {
			throw new CustomerNotFoundException("Customer not found with CF : " + orderDto.getCf());
		}

		if (orderDto.getOrderLines() == null || orderDto.getOrderLines().isEmpty()) {
	        throw new IllegalArgumentException("Order must contain at least one line");
	    }

		if (orderDto.getOrderLines() != null) {
			for (OrderLineDto l : orderDto.getOrderLines()) {
				if (!productRepository.existsByNationalCode(l.getNationalCode())) {
					throw new ProductNotFoundException("Product not found with nationalCode: " + l.getNationalCode());
				}

				Integer quantityOrder = l.getQuantity();
				StockItemDto stock = inventoryRepository.findStockDtoByNationalCode(l.getNationalCode());
				if(stock == null ) throw new StockNotAvailableException("Stock not availabe for product: " + l.getNationalCode());
				if(quantityOrder > stock.getQuantity())
					throw new QuantityNotAvailableException("Quantity not available. Request: " + quantityOrder + ", available: " + stock.getQuantity());
			}
		}

		Order entity = orderMapper.toEntity(orderDto);

		if (entity.getOrderLines() == null)
			entity.setOrderLines(new HashSet<>());

		Order saved = orderRepository.save(entity);

		if (!saved.getOrderLines().isEmpty())
			orderLineRepository.saveAll(saved.getOrderLines());

		for (OrderLineDto l : orderDto.getOrderLines())
			storeMovement(l, MovementType.ORDER_ALLOCATION, -(l.getQuantity()), saved.getId() );

		return orderMapper.toDto(saved);
	}

	@Override
	public OrderDto getOrderById(long id) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

		return orderMapper.toDto(order);
	}

	@Override
	public OrderDto getOrderByCode(String code) {

		Order order = orderRepository.findByCode(code);

		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + code);
		}

		return orderMapper.toDto(order);
	}

	@Override
	public List<OrderDto> getAllOrder() {

		return orderRepository.findAll().stream()
				.map(orderMapper::toDto)
				.toList();
	}

	@Override
	public OrderDto updateState(String code, State newState) {

		Order order = orderRepository.findByCode(code);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + code);
		}

		if (!order.getState().canTransitionTo(newState)) {
		    throw new InvalidStateTransitionException(
		        "Invalid state transition: " + order.getState() + " → " + newState
		    );
		}
		order.setState(newState);
		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	public OrderDto updateStateSafety(String code, State newState) {

		Order order = orderRepository.findByCode(code);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + code);
		}

		if (!order.getState().canTransitionTo(newState)) {
		    throw new InvalidStateTransitionException(
		        "Invalid state transition: " + order.getState() + " → " + newState
		    );
		}
		order.setState(newState);

		if(newState == State.CANCELED) {
			Set<OrderLine> list = order.getOrderLines();

			for(OrderLine line: list)
				storeMovement( orderLineMapper.toDto(line), MovementType.RETURN, line.getQuantity(), order.getId() );
		}

		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	@Transactional
	public OrderDto addLine(String orderCode, OrderLineDto lineDto) {

		if (!productRepository.existsByNationalCode(lineDto.getNationalCode())) {
			throw new ProductNotFoundException("Product not found with nationalCode: " + lineDto.getNationalCode());
		}

		Order order = orderRepository.findByCode(orderCode);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + orderCode);
		}

		if (order.getOrderLines() == null) {
			order.setOrderLines(new HashSet<>());
		}

		OrderLine existing = order.getOrderLines().stream()
				.filter(l -> lineDto.getNationalCode().equals(l.getNationalCode()))
				.findFirst()
				.orElse(null);

		if (existing != null) {
			existing.setQuantity(existing.getQuantity() + lineDto.getQuantity());
			orderLineRepository.save(existing);
		} else {
			OrderLine line = orderLineMapper.toEntity(lineDto);
			line.setOrder(order);
			order.getOrderLines().add(line);
			orderLineRepository.save(line);
		}

		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	@Transactional
	public OrderDto addLineSafety(String orderCode, OrderLineDto lineDto) {

		if (!productRepository.existsByNationalCode(lineDto.getNationalCode())) {
			throw new ProductNotFoundException("Product not found with nationalCode: " + lineDto.getNationalCode());
		}

		Order order = orderRepository.findByCode(orderCode);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + orderCode);
		}

		if(order.getState() != State.OPEN)
			throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state : " + order.getState());

		if (order.getOrderLines() == null) {
			order.setOrderLines(new HashSet<>());
		}

		StockItemDto stock = inventoryRepository.findStockDtoByNationalCode(lineDto.getNationalCode());
		if(stock == null ) throw new StockNotAvailableException("Stock not availabe for product: " + lineDto.getNationalCode());
		if(lineDto.getQuantity() > stock.getQuantity())
			throw new QuantityNotAvailableException("Quantity not available. Request: " + lineDto.getQuantity() + ", available: " + stock.getQuantity());

		storeMovement( lineDto, MovementType.ORDER_ALLOCATION, -(lineDto.getQuantity()),order.getId() );

		OrderLine existing = order.getOrderLines().stream()
				.filter(l -> lineDto.getNationalCode().equals(l.getNationalCode()))
				.findFirst()
				.orElse(null);

		if (existing != null) {
			existing.setQuantity(existing.getQuantity() + lineDto.getQuantity());
			orderLineRepository.save(existing);
		}else {
			OrderLine line = orderLineMapper.toEntity(lineDto);
			line.setOrder(order);
			order.getOrderLines().add(line);
			orderLineRepository.save(line);
		}

		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	@Transactional
	public OrderDto updateLineQuantity(long orderLineId, int newQuantity) {

		OrderLine line = orderLineRepository.findById(orderLineId)
				.orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

		line.setQuantity(newQuantity);
		orderLineRepository.save(line);

		Order order = orderRepository.findById(line.getOrder().getId())
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + line.getOrder().getId()));
		return orderMapper.toDto(order);
	}

	@Override
	@Transactional
	public OrderDto updateLineQuantitySafety(long orderLineId, int newQuantity) {

		OrderLine line = orderLineRepository.findById(orderLineId)
				.orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

		if(line.getOrder().getState() != State.OPEN)
			throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state : " + line.getOrder().getState());

		StockItemDto stock = inventoryRepository.findStockDtoByNationalCode(line.getNationalCode());
		if(stock == null ) throw new StockNotAvailableException("Stock not availabe for product: " + line.getNationalCode());

		InventoryMovement m = new InventoryMovement();
		LocalDateTime now = LocalDateTime.now();

		if(newQuantity > line.getQuantity()) {
			int gap = newQuantity - line.getQuantity();
			if(gap > stock.getQuantity())
				throw new QuantityNotAvailableException("Quantity not available. Current quantity of line: " + line.getQuantity() +", "
						                              + " request more: " + gap + ", available in stock: " + stock.getQuantity());
			m.setQuantity(-gap);
		}else if(newQuantity < line.getQuantity()) {

			int gap = line.getQuantity() - newQuantity;
			m.setQuantity(gap);

		}else
			throw new IllegalArgumentException("Errore insert value. Current quantity = new quantity ");

		m.setNationalCode(line.getNationalCode());
		m.setType(MovementType.ADJUSTMENT);
		m.setReferenceType("ORDER");
		m.setReferenceId(line.getOrder().getId());
		m.setTimestamp(now);
		inventoryRepository.save(m);

		line.setQuantity(newQuantity);
		orderLineRepository.save(line);

		Order order = orderRepository.findById(line.getOrder().getId())
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + line.getOrder().getId()));

		return orderMapper.toDto(order);
	}

	@Override
	@Transactional
	public OrderDto removeLine(long orderLineId) {

		OrderLine line = orderLineRepository.findById(orderLineId)
				.orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

		Order order = line.getOrder();
		if (order == null) {
			throw new InvalidOrderOperationException("The row is not associated with any order");
		}

		if (order.getOrderLines() != null) {
			order.getOrderLines().removeIf(l -> l.getId() == orderLineId);
		}
		orderLineRepository.delete(line);

		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	@Transactional
	public OrderDto removeLineSafety(long orderLineId) {

		OrderLine line = orderLineRepository.findById(orderLineId)
				.orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

		Order order = line.getOrder();
		if (order == null) {
			throw new InvalidOrderOperationException("The row is not associated with any order");
		}

		if(order.getState() != State.OPEN)
			throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state : " + order.getState());

		if (order.getOrderLines() != null) {
			order.getOrderLines().removeIf(l -> l.getId() == orderLineId);
			storeMovement( orderLineMapper.toDto(line), MovementType.ADJUSTMENT, line.getQuantity(), order.getId() );
		}

		orderLineRepository.delete(line);

		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	@Transactional
	public OrderDto clearLines(String orderCode) {

		Order order = orderRepository.findByCode(orderCode);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + orderCode);
		}

		if (order.getOrderLines() != null) {
			order.getOrderLines().clear();
		}

		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	@Transactional
	public OrderDto clearLinesSafety(String orderCode) {

		Order order = orderRepository.findByCode(orderCode);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + orderCode);
		}

		if(order.getState() != State.OPEN)
			throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state : " + order.getState());

		if(order.getOrderLines().isEmpty())
			throw new InvalidOrderOperationException("Orded: " + orderCode + " has nothing to clean. It has no order line.");

		for(OrderLine ol : order.getOrderLines())
			storeMovement( orderLineMapper.toDto(ol), MovementType.ADJUSTMENT, ol.getQuantity(),order.getId() );

		order.getOrderLines().clear();

		return orderMapper.toDto(orderRepository.save(order));
	}

	@Override
	@Transactional
	public OrderDto deleteOrder(String code) {

		Order order = orderRepository.findByCode(code);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + code);
		}

		orderRepository.delete(order);
		return orderMapper.toDto(order);
	}

	@Override
	public OrderDto deleteOrderSafety(String code) {

		Order order = orderRepository.findByCode(code);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with code: " + code);
		}

		State state = order.getState();
		if(state != State.OPEN && state !=State.PENDING)
			throw new CannotDeleteOrderWithOpenOrdersException("Cannot delete order with code: " + code +
		            ". It has already passed to the state SHIPPING. Current state: " + state);

		if(!order.getOrderLines().isEmpty())
			for(OrderLine ol : order.getOrderLines())
				storeMovement( orderLineMapper.toDto(ol), MovementType.RETURN, ol.getQuantity(),order.getId() );

		order.setState(State.CANCELED);
		return orderMapper.toDto(orderRepository.save(order));
	}

	//------> HELEPERS <-------

	void storeMovement(OrderLineDto dto, MovementType type, Integer quantity, Long referenceId) {
		InventoryMovement m = new InventoryMovement();

		LocalDateTime now = LocalDateTime.now();
		m.setNationalCode(dto.getNationalCode());
		m.setQuantity(quantity);
		m.setType( type );
		m.setReferenceType("ORDER");
		m.setReferenceId(referenceId);
		m.setTimestamp(now);
		inventoryRepository.save(m);
	}

}