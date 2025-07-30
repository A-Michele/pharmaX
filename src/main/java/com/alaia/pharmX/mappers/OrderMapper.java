package com.alaia.pharmX.mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.alaia.pharmX.dtos.OrderDto;
import com.alaia.pharmX.models.Order;
import com.alaia.pharmX.models.OrderLine;
import com.alaia.pharmX.dtos.OrderLineDto;

public class OrderMapper {

	public static OrderDto toDto(Order order) {
        if (order == null) return null;

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCode(order.getCode());
        dto.setState(order.getState());
        dto.setCF(order.getCF());

        if (order.getOrderLines() != null) {
            Set<OrderLineDto> lines = order.getOrderLines().stream()
                .map(OrderLineMapper::toDto)
                .collect(Collectors.toSet());

            dto.setOrderLines(lines);
        }

        return dto;
    }

	 public static Order toEntity(OrderDto dto) {
	        if (dto == null) return null;

	        Order order = new Order();
	        order.setId(dto.getId());
	        order.setCode(dto.getCode());
	        order.setState(dto.getState());
	        order.setCF(dto.getCF());

	        if (dto.getOrderLines() != null) {
	            Set<OrderLine> lines = new HashSet<>();

	            for (OrderLineDto lineDto : dto.getOrderLines()) {
	                OrderLine line = OrderLineMapper.toEntity(lineDto);

	                //Evitiamo il loop: settiamo manualmente l’order su ogni riga
	                line.setOrder(order);
	                lines.add(line);
	            }

	            order.setOrderLines(lines);
	        }

	        return order;
	    }
}
