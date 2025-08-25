package com.alaia.pharmX.services;

import java.util.List;

import com.alaia.pharmX.dtos.OrderDto;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.models.State;

public interface OrderService {

    OrderDto getOrderById(long id);
    OrderDto getOrderByCode(String code);
    List<OrderDto> getAllOrder();

    OrderDto createOrder(OrderDto orderDto);
    OrderDto updateState(String code, State newState);
    OrderDto addLine(String orderCode, OrderLineDto lineDto);
    OrderDto updateLineQuantity(long orderLineId, int newQuantity);
    OrderDto removeLine(long orderLineId);
    OrderDto clearLines(String orderCode);
    OrderDto deleteOrder(String code);

    OrderDto deleteOrderSafety(String code);
    OrderDto createOrderSafety(OrderDto orderDto);
	OrderDto updateStateSafety(String code, State newState);
	OrderDto addLineSafety(String orderCode, OrderLineDto lineDto);
	OrderDto updateLineQuantitySafety(long orderLineId, int newQuantity);
	OrderDto removeLineSafety(long orderLineId);
	OrderDto clearLinesSafety(String orderCode);

}