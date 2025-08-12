package com.alaia.pharmX.servicesImpl;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.mappers.OrderLineMapper;
import com.alaia.pharmX.models.Order;
import com.alaia.pharmX.models.OrderLine;
import com.alaia.pharmX.repositories.OrderLineRepository;
import com.alaia.pharmX.repositories.OrderRepository;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.services.OrderLineService;
import com.alaia.pharmX.servicesImpl.exceptions.OrderLineNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductNotFoundException;

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
}
