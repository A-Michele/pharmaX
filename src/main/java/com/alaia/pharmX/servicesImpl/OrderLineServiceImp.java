package com.alaia.pharmX.servicesImpl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.dtos.receiving.StockItemDto;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.OrderLineMapper;
import com.alaia.pharmX.models.Order;
import com.alaia.pharmX.models.OrderLine;
import com.alaia.pharmX.models.State;
import com.alaia.pharmX.models.receiving.InventoryMovement;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.OrderLineRepository;
import com.alaia.pharmX.repositories.OrderRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.receiving.InventoryMovementRepository;
import com.alaia.pharmX.services.OrderLineService;

import jakarta.transaction.Transactional;

@Service
public class OrderLineServiceImp implements OrderLineService{

	@Autowired
	private ProductRepository productRepository;

	@Autowired
    private OrderLineMapper orderLineMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderLineRepository orderLineRepository;

	@Autowired
	private InventoryMovementRepository inventoryRepository;

	@Override
    @Transactional
    public OrderLineDto createForOrder(String orderCode, OrderLineDto lineDto) {

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
                .filter(l -> l.getNationalCode().equals(lineDto.getNationalCode()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + lineDto.getQuantity());
            OrderLine saved = orderLineRepository.save(existing);
            return orderLineMapper.toDto(saved);
        }

        OrderLine entity = orderLineMapper.toEntity(lineDto);
        entity.setOrder(order);
        order.getOrderLines().add(entity);
        OrderLine saved = orderLineRepository.save(entity);
        return orderLineMapper.toDto(saved);

    }

	@Override
    @Transactional
    public OrderLineDto createForOrderSafety(String orderCode, OrderLineDto lineDto) {

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

		storeMovement( lineDto, MovementType.ORDER_ALLOCATION, -(lineDto.getQuantity()), order.getId() );

        OrderLine existing = order.getOrderLines().stream()
                .filter(l -> l.getNationalCode().equals(lineDto.getNationalCode()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + lineDto.getQuantity());
            OrderLine saved = orderLineRepository.save(existing);
            return orderLineMapper.toDto(saved);
        }

        OrderLine entity = orderLineMapper.toEntity(lineDto);
        entity.setOrder(order);
        order.getOrderLines().add(entity);

        OrderLine saved = orderLineRepository.save(entity);
        return orderLineMapper.toDto(saved);
    }

	@Override
    public OrderLineDto getById(long id) {

        OrderLine line = orderLineRepository.findById(id)
                .orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + id));

        return orderLineMapper.toDto(line);
    }

	@Override
	public List<OrderLineDto> getByOrderCode(String orderCode) {

		return orderLineRepository.findByOrder_Code(orderCode)
				.stream()
				.map(orderLineMapper::toDto)
				.toList();
	}

	@Override
	@Transactional
	public OrderLineDto updateQuantity(long orderLineId, int newQuantity) {

		OrderLine line = orderLineRepository.findById(orderLineId)
				.orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

		line.setQuantity(newQuantity);
		OrderLine saved = orderLineRepository.save(line);
		return orderLineMapper.toDto(saved);
	}

	@Override
	@Transactional
	public OrderLineDto updateQuantitySafety(long orderLineId, int newQuantity) {

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
				throw new QuantityNotAvailableException("Quantity not available. Current quantity of line: " + line.getQuantity() +", request more: " + gap + ", available in stock: " + stock.getQuantity());

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
		OrderLine saved = orderLineRepository.save(line);
		return orderLineMapper.toDto(saved);
	}

	@Override
	@Transactional
	public OrderLineDto delete(long orderLineId) {

		OrderLine line = orderLineRepository.findById(orderLineId)
				.orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

		Order order = line.getOrder();
		if (order != null && order.getOrderLines() != null) {
			order.getOrderLines().remove(line);
		}

		orderLineRepository.delete(line);
		return orderLineMapper.toDto(line);
	}

	@Override
	@Transactional
	public OrderLineDto deleteSafety(long orderLineId) {

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
		return orderLineMapper.toDto(line);
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