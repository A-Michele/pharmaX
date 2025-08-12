package com.alaia.pharmX.services;

import java.util.List;

import com.alaia.pharmX.dtos.OrderDto;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.models.State;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);
    OrderDto getOrderById(long id);
    OrderDto getOrderByCode(String code);
    List<OrderDto> getAllOrder();
    OrderDto updateState(String code, State newState);

    OrderDto addLine(String orderCode, OrderLineDto lineDto);
    OrderDto updateLineQuantity(long orderLineId, int newQuantity);
    OrderDto removeLine(long orderLineId);
    OrderDto clearLines(String orderCode);

    OrderDto deleteOrder(String code);
}
