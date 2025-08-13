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

import com.alaia.pharmX.dtos.SlotDto;
import com.alaia.pharmX.dtos.SlotPatchDto;
import com.alaia.pharmX.services.SlotService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/slot")
public class SlotController {

	@Autowired
	private SlotService slotService;

	@PostMapping
	public ResponseEntity<SlotDto> create(
			@RequestBody @Valid SlotDto dto,
			@RequestParam Long sectionId) {
		SlotDto created = slotService.create(dto, sectionId);
		return new ResponseEntity<>(created, HttpStatus.OK);
	}

	@GetMapping("/{id}")
    public ResponseEntity<SlotDto> getById(@PathVariable int id) {
		SlotDto slotDto = slotService.getById(id);
		return new ResponseEntity<>(slotDto, HttpStatus.OK);
    }

	@GetMapping("/by-code/{code}")
    public ResponseEntity<SlotDto> getByCode(@PathVariable String code) {
		SlotDto slotDto = slotService.getByCode(code);
		return new ResponseEntity<>(slotDto, HttpStatus.OK);
    }

	@GetMapping("/all")
	public ResponseEntity<List<SlotDto>> getAllSlots() {
		List<SlotDto> slotDto = slotService.getAllSlots();
		return new ResponseEntity<>(slotDto, HttpStatus.OK);
	}

	@PatchMapping("/{id}")
    public ResponseEntity<SlotDto> update(@PathVariable long id, @RequestBody @Valid SlotPatchDto  dto) {
		SlotDto slotDto =  slotService.patchSlot(id, dto);
		return new ResponseEntity<>(slotDto, HttpStatus.OK);
    }

	@DeleteMapping("/{id}")
    public ResponseEntity<SlotDto> delete(@PathVariable long id) {
		SlotDto slotDto = slotService.delete(id);
		return new ResponseEntity<>(slotDto, HttpStatus.OK);
    }

	@PostMapping("/{slotId}/move")
    public ResponseEntity<SlotDto> move(@PathVariable long slotId, @RequestParam int targetSectionId) {
		SlotDto slotDto = slotService.move(slotId, targetSectionId);
		return new ResponseEntity<>(slotDto, HttpStatus.OK);
    }
}
