package com.alaia.pharmX.mappers.order;

import org.springframework.stereotype.Component;

import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.OrderLine;

import lombok.AllArgsConstructor;
@AllArgsConstructor
@Component
public class OrderLineMapper {

    public OrderLineDto toDto(OrderLine orderLine) {
        if (orderLine == null) return null;

        OrderLineDto dto = new OrderLineDto();
        dto.setId(orderLine.getId());
        dto.setNationalCode(orderLine.getNationalCode());
        dto.setQuantity(orderLine.getQuantity());
        dto.setLineNumber(orderLine.getLineNumber());
        dto.setType(orderLine.getType());
        return dto;
    }

    public OrderLine toEntity(OrderLineDto dto) {
        if (dto == null) return null;

        OrderLine orderLine = new OrderLine();
        orderLine.setId(dto.getId());
        orderLine.setNationalCode(dto.getNationalCode());
        orderLine.setQuantity(dto.getQuantity());
        if(orderLine.getType() == null)
        	orderLine.setType(LineOrderType.OPEN);
		if(orderLine.getLineNumber() == null) {
			String rand = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
			orderLine.setLineNumber("ORDLINE-" + rand);
		}
        return orderLine;
    }
}
