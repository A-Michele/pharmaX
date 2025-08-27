package com.alaia.pharmX.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.OrderDto;
import com.alaia.pharmX.dtos.OrderLineDto;
import com.alaia.pharmX.services.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderDto orderDto) {
        OrderDto saved = orderService.createOrder(orderDto);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

	@GetMapping("/order_id/{id}")
	public ResponseEntity<OrderDto> getCustomerById(@PathVariable long id) {
		OrderDto orderDto =  orderService.getOrderById(id);
		return new ResponseEntity<>(orderDto, HttpStatus.OK);
	}

	@GetMapping("/order_code/{code}")
	public ResponseEntity<OrderDto> getCustomerByCode(@PathVariable @NotBlank String code) {
		OrderDto orderDto =  orderService.getOrderByCode(code);
		return new ResponseEntity<>(orderDto, HttpStatus.OK);
	}

	@GetMapping("/all")
	public ResponseEntity<List<OrderDto>> getAllOrders() {
		List<OrderDto> orderDto = orderService.getAllOrder();
		return new ResponseEntity<>(orderDto, HttpStatus.OK);
	}

	@PostMapping("addLine/{code}")
	public ResponseEntity<OrderDto> addLine(@PathVariable @NotBlank String code,
			                                @Valid @RequestBody OrderLineDto lineDto) {
		OrderDto updated = orderService.addLine(code, lineDto);
		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	@PatchMapping("/lines/{orderLineId}/update")
	public ResponseEntity<OrderDto> updateLineQuantity(@PathVariable long orderLineId,
													   @Positive @RequestParam int quantity) {
		return ResponseEntity.ok(orderService.updateLineQuantity(orderLineId, quantity));
	}

	@DeleteMapping("/lines/{orderLineId}")
    public ResponseEntity<OrderDto> removeLine(@PathVariable long orderLineId) {
		OrderDto orderDto = orderService.removeLine(orderLineId);
        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }

	@DeleteMapping("/clearOrder/{code}")
	public ResponseEntity<OrderDto> clearLines(@PathVariable @NotBlank String code) {
		OrderDto orderDto = orderService.clearLines(code);
		return new ResponseEntity<>(orderDto, HttpStatus.OK);
	}

	@PatchMapping("/delete/{code}")
    public ResponseEntity<OrderDto> delete(@PathVariable String code) {
		OrderDto orderDto = orderService.deleteOrder(code);
		return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }
}