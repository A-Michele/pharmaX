package com.alaia.pharmX.mappers;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.models.OrderLine;

@Component
public class OrderLineMapper {

    public OrderLineDto toDto(OrderLine orderLine) {
        if (orderLine == null) return null;

        OrderLineDto dto = new OrderLineDto();
        dto.setId(orderLine.getId());
        dto.setNationalCode(orderLine.getNationalCode());
        dto.setQuantity(orderLine.getQuantity());

        //NON mappiamo l'Order, per evitare loop
        return dto;
    }

    public OrderLine toEntity(OrderLineDto dto) {
        if (dto == null) return null;

        OrderLine orderLine = new OrderLine();
        orderLine.setId(dto.getId());
        orderLine.setNationalCode(dto.getNationalCode());
        orderLine.setQuantity(dto.getQuantity());

        //L'ordine va impostato a parte nel contesto del ciclo in OrderMapper
        return orderLine;
    }
}
