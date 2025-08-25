package com.alaia.pharmX.controllers.receiving;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.receiving.LotDto;
import com.alaia.pharmX.services.receiving.LotService;

@RestController
@RequestMapping("/lot")
class LotController {

	@Autowired
    private LotService lotService;

	@GetMapping("/{lotCode}")
	public ResponseEntity<LotDto> byCode(@PathVariable String lotCode) {
		LotDto lotDto = lotService.getByCode(lotCode);
		return new ResponseEntity<>(lotDto, HttpStatus.OK);
	}
}