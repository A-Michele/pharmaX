package com.alaia.pharmX.servicesImpl;

import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockOperation;
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
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.OrderLineRepository;
import com.alaia.pharmX.repositories.OrderRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.services.OrderLineService;
import com.alaia.pharmX.services.stock.StockService;

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
    @Transactional
    public OrderLineDto createForOrder(String orderCode, OrderLineDto lineDto) {

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

        StockOperation ope = buildStockOperation(lineDto, MovementType.ORDER_ALLOCATION, lineDto.getQuantity(), order.getId());
		stockService.reserveQuantity(ope);

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

		if(line.getOrder().getState() != State.OPEN)
			throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state : " + line.getOrder().getState());

		AvailableQuantityProduct aqP = stockService.getAvailableQuantity(line.getNationalCode());
		if(aqP == null ) throw new StockNotAvailableException("Stock not availabe for product: " + line.getNationalCode());

		int gap = 0;
		int flag = 0;

		if(newQuantity > line.getQuantity()) {
			gap = newQuantity - line.getQuantity();

			if(gap > aqP.getAvailableQuantity())
				throw new QuantityNotAvailableException("Quantity not available. Current quantity of line: " + line.getQuantity() +", request more: " + gap + ", available in stock: " + aqP.getAvailableQuantity());

		}else if(newQuantity < line.getQuantity()) {
			flag = 1;
			gap = line.getQuantity() - newQuantity;
		}else
			throw new IllegalArgumentException("Errore insert value. Current quantity = new quantity ");

		StockOperation ope = buildStockOperation(orderLineMapper.toDto(line), MovementType.ADJUSTMENT, gap, line.getOrder().getId());
		if(flag == 1) stockService.unReserveQuantityOnDeleteOrCanceled(ope);
		else	stockService.reserveQuantity(ope);

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
		if (order == null) {
			throw new InvalidOrderOperationException("The row is not associated with any order");
		}

		if(order.getState() != State.OPEN)
			throw new InvalidUpdateQuantityException("Can only update if the order is in status: OPEN Current order state : " + order.getState());

		if (order.getOrderLines() != null) {
			order.getOrderLines().removeIf(l -> l.getId() == orderLineId);

			StockOperation ope = buildStockOperation(orderLineMapper.toDto(line), MovementType.RETURN, line.getQuantity(), order.getId());
			stockService.unReserveQuantityOnDeleteOrCanceled(ope);
		}

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