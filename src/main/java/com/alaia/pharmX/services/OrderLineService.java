package com.alaia.pharmX.services;

import java.util.List;
import com.alaia.pharmX.dtos.OrderLineDto;

public interface OrderLineService {

	OrderLineDto createForOrder(String orderCode, OrderLineDto lineDto);
    OrderLineDto getById(long id);
    List<OrderLineDto> getByOrderCode(String orderCode);
    OrderLineDto updateQuantity(long orderLineId, int newQuantity);
    OrderLineDto delete(long orderLineId);

}