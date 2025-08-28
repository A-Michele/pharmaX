package com.alaia.pharmX.services.order;

import java.util.List;

import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;

public interface OrderService {

    OrderDto getOrderById(long id);
    OrderDto getOrderByCode(String code);
    List<OrderDto> getAllOrder();

    OrderDto deleteOrder(String code);
    OrderDto createOrder(OrderDto orderDto);
	OrderDto addLine(String orderCode, OrderLineDto lineDto);
	OrderDto updateLineQuantity(long orderLineId, int newQuantity);
	OrderDto removeLine(long orderLineId);
	OrderDto clearLines(String orderCode);

}