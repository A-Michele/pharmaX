package com.alaia.pharmX.controllers.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.services.order.OrderLineService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/orderLine")
@Validated
public class OrderLineController {

	@Autowired
	private OrderLineService orderLineService;

	@PostMapping
	public ResponseEntity<OrderLineDto> createForOrder(@RequestParam @NotBlank String orderCode,
													   @PositiveOrZero @RequestParam OrderLineDto lineDto) {
		OrderLineDto orderLineDto = orderLineService.createForOrder(orderCode, lineDto);
		return new ResponseEntity<>(orderLineDto, HttpStatus.OK);
	}

    @GetMapping("/{id}")
    public ResponseEntity<OrderLineDto> getById(@PathVariable long id) {
    	OrderLineDto orderLineDto = orderLineService.getById(id);
    	return new ResponseEntity<>(orderLineDto, HttpStatus.OK);
    }

    @GetMapping("/by-order/{code}")
    public ResponseEntity<List<OrderLineDto>> getByOrderCode(@PathVariable @NotBlank String code) {
    	List<OrderLineDto> orderLineDto = orderLineService.getByOrderCode(code);
    	return new ResponseEntity<>(orderLineDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<OrderLineDto> updateQuantity(@PathVariable long id,
                                                       @RequestParam @PositiveOrZero int quantity) {
    	OrderLineDto orderLineDto = orderLineService.updateQuantity(id, quantity);
    	return new ResponseEntity<>(orderLineDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderLineDto> delete(@PathVariable long id) {
    	OrderLineDto deleteOrderLine = orderLineService.delete(id);
    	return new ResponseEntity<>(deleteOrderLine, HttpStatus.OK);
    }
}