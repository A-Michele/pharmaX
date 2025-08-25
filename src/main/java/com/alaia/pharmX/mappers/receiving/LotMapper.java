package com.alaia.pharmX.mappers.receiving;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.receiving.LotDto;
import com.alaia.pharmX.models.receiving.Lot;

@Component
public class LotMapper {
	public LotDto toDto(Lot lot) {
		if (lot == null) return null;
		LotDto dto = new LotDto();
		dto.setId(lot.getId() == 0L ? null : lot.getId());
		dto.setLotCode(lot.getLotCode());
		dto.setExpiryDate(lot.getExpiryDate());
		dto.setNotes(lot.getNotes());
		dto.setQuantity(lot.getQuantity());
		return dto;
	}

	public Lot toEntity(LotDto dto) {
		if (dto == null) return null;
		Lot e = new Lot();
		if (dto.getId() != null) e.setId(dto.getId());
		e.setLotCode(dto.getLotCode());
		e.setExpiryDate(dto.getExpiryDate());
		e.setNotes(dto.getNotes());
		e.setQuantity(dto.getQuantity());
		return e;
	}
}