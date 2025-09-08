package com.alaia.pharmX.mappers.order;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;

import lombok.AllArgsConstructor;
@AllArgsConstructor
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
        dto.setDate(order.getDate());

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
		order.setDate(dto.getDate());

		if (dto.getOrderLines() != null) {
			Set<OrderLine> lines = new HashSet<>();

			for (OrderLineDto lineDto : dto.getOrderLines()) {
				if(lineDto.getType() == null)
					lineDto.setType(LineOrderType.OPEN);
				if(lineDto.getLineNumber() == null) {
					String rand = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
					lineDto.setLineNumber("ORDLINE-" + rand);
				}
				OrderLine line = orderLineMapper.toEntity(lineDto);
				line.setOrder(order);
				lines.add(line);
			}

			order.setOrderLines(lines);
		}

		return order;
	}
}