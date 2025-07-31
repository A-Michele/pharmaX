package com.alaia.pharmX.mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.OrderDto;
import com.alaia.pharmX.models.Order;
import com.alaia.pharmX.models.OrderLine;
import com.alaia.pharmX.dtos.OrderLineDto;

@Component
public class OrderMapper {

	@Autowired
    private OrderLineMapper orderLineMapper;

	public OrderDto toDto(Order order) {
        if (order == null) return null;

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCode(order.getCode());
        dto.setState(order.getState());
        dto.setCf(order.getCf());

        if (order.getOrderLines() != null) {
            Set<OrderLineDto> lines = order.getOrderLines().stream()
                .map(orderLineMapper::toDto)
                .collect(Collectors.toSet());

            dto.setOrderLines(lines);
        }

        return dto;
    }

	 public Order toEntity(OrderDto dto) {
	        if (dto == null) return null;

	        Order order = new Order();
	        order.setId(dto.getId());
	        order.setCode(dto.getCode());
	        order.setState(dto.getState());
	        order.setCf(dto.getCf());

	        if (dto.getOrderLines() != null) {
	            Set<OrderLine> lines = new HashSet<>();

	            for (OrderLineDto lineDto : dto.getOrderLines()) {
	                OrderLine line = orderLineMapper.toEntity(lineDto);

	                //Evitiamo il loop: settiamo manualmente l’order su ogni riga
	                line.setOrder(order);
	                lines.add(line);
	            }

	            order.setOrderLines(lines);
	        }

	        return order;
	    }
}
