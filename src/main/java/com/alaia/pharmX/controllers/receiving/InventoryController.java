package com.alaia.pharmX.controllers.receiving;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.receiving.MovementDto;
import com.alaia.pharmX.dtos.receiving.StockBySlotDto;
import com.alaia.pharmX.dtos.receiving.StockItemDto;
import com.alaia.pharmX.services.receiving.InventoryService;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	private InventoryService inventoryService;

	@GetMapping("/movements/receipt/{receiptId}")
	public ResponseEntity<List<MovementDto>> byReceipt(@PathVariable Long receiptId) {
		List<MovementDto> movement = inventoryService.getMovementsByReceipt(receiptId);
		return new ResponseEntity<>(movement, HttpStatus.OK);
	}

	@GetMapping("/movements/order/{orderId}")
	public ResponseEntity<List<MovementDto>> byOrder(@PathVariable Long orderId) {
		List<MovementDto> movement = inventoryService.getMovementsByOrder(orderId);
		return new ResponseEntity<>(movement, HttpStatus.OK);
	}

	@GetMapping("/stock")
	public ResponseEntity<List<StockItemDto>> stock() {
		List<StockItemDto> stock = inventoryService.getStock();
		return new ResponseEntity<>(stock, HttpStatus.OK);
	}

	@GetMapping("/stock/by-asOfDate")
	public ResponseEntity<List<StockItemDto>> getStockAsOf(
			@RequestParam("at")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf) {
		List<StockItemDto> stock = inventoryService.getStockAsOf(asOf);
		return new ResponseEntity<>(stock, HttpStatus.OK);
	}

	@GetMapping("/movements")
	public ResponseEntity<List<MovementDto>> byNationalCode(@RequestParam String nationalCode) {
		List<MovementDto> movement = inventoryService.getMovementsByNationalCode(nationalCode);
		return new ResponseEntity<>(movement, HttpStatus.OK);
	}

	@GetMapping("/stock/by-nationalCode")
	public ResponseEntity<StockItemDto> stockByNationalCode(@RequestParam String nationalCode) {
		StockItemDto stock = inventoryService.getStokOfNationalCode(nationalCode);
		return new ResponseEntity<>(stock, HttpStatus.OK);
	}

	@GetMapping("/stock/by-slot")
	public ResponseEntity<List<StockBySlotDto>> stockBySlot() {
		List<StockBySlotDto> stock = inventoryService.getStockBySlot();
		return new ResponseEntity<>(stock, HttpStatus.OK);
	}
}