package com.alaia.pharmX.servicesImpl.order;

import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.order.OrderLineMapper;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.services.order.OrderLineService;
import com.alaia.pharmX.services.stock.StockService;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
	private StockService stockService;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderLineDto createForOrder(String orderCode, OrderLineDto lineDto) {
	    // Validation
	    Order order = validateOrderAndProduct(orderCode, lineDto);

	    // Stock Reservation
	    reserveStockForOrder(lineDto, order);

	    // Saving
	    return saveOrderLine(lineDto, order);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderLineDto getById(long id) {

        OrderLine line = orderLineRepository.findById(id)
                .orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + id));

        return orderLineMapper.toDto(line);
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<OrderLineDto> getByOrderCode(String orderCode) {

		return orderLineRepository.findByOrder_Code(orderCode)
				.stream()
				.map(orderLineMapper::toDto)
				.toList();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderLineDto updateQuantity(long orderLineId, int newQuantity) {
	    // Validation
	    OrderLine line = validateOrderLineAndQuantity(orderLineId, newQuantity);

	    // Stock Operation
	    adjustStockForQuantityUpdate(line, newQuantity);

	    // Saving
	    return saveUpdatedOrderLine(line, newQuantity);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public OrderLineDto delete(long orderLineId) {
		// Validation
		OrderLine line = validateOrderLineAndOrder(orderLineId);

		// Stock Operation
		unreserveStockForDeletion(line);

		// Deletion
		return deleteOrderLine(line);
	}

	//------> HELEPERS FOR CREATE ORDER WITH ORDER LINE <-------

	private Order validateOrderAndProduct(String orderCode, OrderLineDto lineDto) {
	    if (!productRepository.existsByNationalCode(lineDto.getNationalCode())) {
	        throw new ProductNotFoundException("Product not found with nationalCode: " + lineDto.getNationalCode());
	    }

	    Order order = orderRepository.findByCode(orderCode);
	    if (order == null) {
	        throw new OrderNotFoundException("Order not found with code: " + orderCode);
	    }

	    if (order.getState() != State.OPEN) {
	        throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state: " + order.getState());
	    }

	    return order;
	}

	private void reserveStockForOrder(OrderLineDto lineDto, Order order) {
	    StockOperation ope = buildStockOperation(lineDto, MovementType.ORDER_ALLOCATION, lineDto.getQuantity(), order.getId());
	    stockService.reserveQuantity(ope);
	}

	private OrderLineDto saveOrderLine(OrderLineDto lineDto, Order order) {
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

	//------> HELEPERS FOR UPDATE QUANTITY TO ORDER LINE <-------

	private OrderLine validateOrderLineAndQuantity(long orderLineId, int newQuantity) {
	    OrderLine line = orderLineRepository.findById(orderLineId)
	            .orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

	    if (line.getOrder().getState() != State.OPEN) {
	        throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state: " + line.getOrder().getState());
	    }

	    AvailableQuantityProduct aqP = stockService.getAvailableQuantity(line.getNationalCode());
	    if (aqP == null) {
	        throw new StockNotAvailableException("Stock not available for product: " + line.getNationalCode());
	    }

	    if (newQuantity > line.getQuantity()) {
	        int gap = newQuantity - line.getQuantity();
	        if (gap > aqP.getAvailableQuantity()) {
	            throw new QuantityNotAvailableException("Quantity not available. Current quantity of line: " + line.getQuantity() + ", request more: " + gap + ", available in stock: " + aqP.getAvailableQuantity());
	        }
	    } else if (newQuantity < line.getQuantity()) {
	        // Valid case, will handle in stock operation
	    } else {
	        throw new IllegalArgumentException("Error: insert value. Current quantity = new quantity");
	    }

	    return line;
	}

	private void adjustStockForQuantityUpdate(OrderLine line, int newQuantity) {
	    int gap = Math.abs(newQuantity - line.getQuantity());
	    MovementType movementType = (newQuantity > line.getQuantity()) ? MovementType.ADJUSTMENT : MovementType.ADJUSTMENT;
	    StockOperation ope = buildStockOperation(orderLineMapper.toDto(line), movementType, gap, line.getOrder().getId());

	    if (newQuantity > line.getQuantity()) {
	        stockService.reserveQuantity(ope);
	    } else {
	        stockService.unReserveQuantityOnDeleteOrCanceled(ope);
	    }
	}

	private OrderLineDto saveUpdatedOrderLine(OrderLine line, int newQuantity) {
	    line.setQuantity(newQuantity);
	    OrderLine saved = orderLineRepository.save(line);
	    return orderLineMapper.toDto(saved);
	}

	//------> HELEPERS FOR DELETE ORDER LINE <-------

	private OrderLine validateOrderLineAndOrder(long orderLineId) {
	    OrderLine line = orderLineRepository.findById(orderLineId)
	            .orElseThrow(() -> new OrderLineNotFoundException("OrderLine not found with id: " + orderLineId));

	    Order order = line.getOrder();
	    if (order == null) {
	        throw new InvalidOrderOperationException("The row is not associated with any order");
	    }

	    if (order.getState() != State.OPEN) {
	        throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state: " + order.getState());
	    }

	    return line;
	}

	private void unreserveStockForDeletion(OrderLine line) {
	    Order order = line.getOrder();
	    if (order.getOrderLines() != null) {
	        order.getOrderLines().removeIf(l -> l.getId() == line.getId());
	        StockOperation ope = buildStockOperation(orderLineMapper.toDto(line), MovementType.RETURN, line.getQuantity(), order.getId());
	        stockService.unReserveQuantityOnDeleteOrCanceled(ope);
	    }
	}

	private OrderLineDto deleteOrderLine(OrderLine line) {
	    orderLineRepository.delete(line);
	    return orderLineMapper.toDto(line);
	}

	//------> HELEPERS <-------

	StockOperation buildStockOperation(OrderLineDto dto, MovementType type, Integer quantity, Long referenceId) {
		StockOperation ope = new StockOperation();
		ope.setNationalCode(dto.getNationalCode());
		ope.setReferenceType("ORDER");
		ope.setReferenceId(referenceId);
		ope.setType(type);
		ope.setQuantity(quantity);
		return ope;
	}
}