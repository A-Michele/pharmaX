package com.alaia.pharmX.controllers.stock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.EffectiveQuantityProduct;
import com.alaia.pharmX.dtos.stock.ReservedQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockDto;
import com.alaia.pharmX.services.stock.StockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/stock")
public class StockController {

	@Autowired
    private StockService stockService;

	@GetMapping("/effective-quantity")
	public ResponseEntity<EffectiveQuantityProduct> effectiveQuantity(@RequestParam String nationalCode) {
		EffectiveQuantityProduct eqp = stockService.getEffectiveQuantity(nationalCode);
		return new ResponseEntity<>(eqp, HttpStatus.OK);
	}

	@GetMapping("/reserved-quantity")
	public ResponseEntity<ReservedQuantityProduct> reservedQuantity(@RequestParam String nationalCode) {
		ReservedQuantityProduct rqp = stockService.getReservedQuantity(nationalCode);
		return new ResponseEntity<>(rqp, HttpStatus.OK);
	}

	@GetMapping("/available-quantity")
	public ResponseEntity<AvailableQuantityProduct> availableQuantity(@RequestParam String nationalCode) {
		AvailableQuantityProduct aqp = stockService.getAvailableQuantity(nationalCode);
		return new ResponseEntity<>(aqp, HttpStatus.OK);
	}

	@PostMapping
    public ResponseEntity<StockDto> create(@Valid @RequestBody StockDto stock) {
		StockDto stockDto = stockService.createStock(stock);
		return new ResponseEntity<>(stockDto, HttpStatus.CREATED);
    }

	@GetMapping("/all")
    public ResponseEntity<List<StockDto>> getAll() {
		List<StockDto> stocks = stockService.getAllStock();
		return new ResponseEntity<>(stocks, HttpStatus.OK);
    }

	@PatchMapping("/update-effectiveQuantity")
    public ResponseEntity<StockDto> updateEffectiveQuantity(@RequestBody EffectiveQuantityProduct eqP) {
		StockDto stock = stockService.updateEffectiveQuantity(eqP);
		return new ResponseEntity<>(stock, HttpStatus.OK);
    }
}