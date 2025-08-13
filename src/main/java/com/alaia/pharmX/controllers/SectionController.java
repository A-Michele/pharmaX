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
import org.springframework.web.bind.annotation.RestController;

import com.alaia.pharmX.dtos.SectionDto;
import com.alaia.pharmX.dtos.SectionUpdateDto;
import com.alaia.pharmX.services.SectionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/section")
public class SectionController {

	@Autowired
	private SectionService sectionService;

	@PostMapping
    public ResponseEntity<SectionDto> create(@RequestBody @Valid SectionDto dto) {
        SectionDto created = sectionService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

	@GetMapping("/{id}")
    public ResponseEntity<SectionDto> getById(@PathVariable int id) {
		SectionDto sectionDto =  sectionService.getById(id);
		return new ResponseEntity<>(sectionDto, HttpStatus.OK);
    }

	@GetMapping("/by-code/{code}")
    public ResponseEntity<SectionDto> getByCode(@PathVariable String code) {
		SectionDto sectionDto =  sectionService.getByCode(code);
		return new ResponseEntity<>(sectionDto, HttpStatus.OK);
    }

	@GetMapping("/all")
	public ResponseEntity<List<SectionDto>> getAllSections() {
		List<SectionDto> sectionDto = sectionService.getAllSection();
		return new ResponseEntity<>(sectionDto, HttpStatus.OK);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<SectionDto> patchNameAndCategory(@PathVariable int id,
	        											   @RequestBody @Valid SectionUpdateDto dto) {
	    SectionDto updated = sectionService.updateNameAndCategory(id, dto);
	    return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
    public ResponseEntity<SectionDto> delete(@PathVariable int id) {
		SectionDto sectionDto = sectionService.delete(id);
		return new ResponseEntity<>(sectionDto, HttpStatus.OK);
    }

	@PostMapping("/{sectionId}/slots/{slotId}")
    public ResponseEntity<SectionDto> addExistingSlot(@PathVariable int sectionId, @PathVariable long slotId) {
		SectionDto sectionDto = sectionService.addExistingSlot(sectionId, slotId);
		return new ResponseEntity<>(sectionDto, HttpStatus.OK);
    }

	@DeleteMapping("/{sectionId}/slots/{slotId}")
    public ResponseEntity<SectionDto> removeSlot(@PathVariable int sectionId, @PathVariable long slotId) {
		SectionDto sectionDto = sectionService.removeSlot(sectionId, slotId);
		return new ResponseEntity<>(sectionDto, HttpStatus.OK);
    }
}
