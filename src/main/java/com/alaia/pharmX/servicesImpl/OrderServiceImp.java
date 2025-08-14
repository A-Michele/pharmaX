package com.alaia.pharmX.servicesImpl;

import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.alaia.pharmX.services.OrderService;
import com.alaia.pharmX.servicesImpl.exceptions.CannotDeleteOrderWithOpenOrdersException;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.InvalidOrderOperationException;
import com.alaia.pharmX.servicesImpl.exceptions.InvalidStateTransitionException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderLineNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImp implements OrderService{

	@Autowired
	private ProductRepository productRepository;

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
	@Transactional
	public OrderDto createOrder(OrderDto orderDto) {
		if (orderRepository.existsByCode(orderDto.getCode())) {
			throw new OrderAlreadyExistsException("Order already exists with code: " + orderDto.getCode());
		}

		Customer customer = customerRepository.findByCf(orderDto.getCf());
		if (customer == null) {
			throw new CustomerNotFoundException("Customer not found with CF : " + orderDto.getCf());
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
	public OrderDto removeLine(long orderLineId) {
		OrderLine line = orderLineRepository.findById(orderLineId)
				.orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

		Order order = line.getOrder();
		if (order == null) {
			throw new InvalidOrderOperationException("La riga non è associata ad alcun ordine");
		}

		if (order.getOrderLines() != null) {
			order.getOrderLines().removeIf(l -> l.getId() == orderLineId);
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

		orderRepository.delete(order);
		return orderMapper.toDto(order);
	}

}
