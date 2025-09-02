package com.alaia.pharmX.mappers.stock;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.stock.InfoSlotDto;
import com.alaia.pharmX.models.stock.InfoSlot;

@Component
public class InfoSlotMapper {
	public InfoSlotDto toDto(InfoSlot infoSlot) {
        if (infoSlot == null) return null;

        InfoSlotDto dto = new InfoSlotDto();
        dto.setSlotCode(infoSlot.getSlotCode());
        dto.setQuantity(infoSlot.getQuantity());
        return dto;
    }

	public InfoSlot toEntity(InfoSlotDto dto) {
        if (dto == null) return null;

        InfoSlot infoSlot = new InfoSlot();
        infoSlot.setSlotCode(dto.getSlotCode());
        infoSlot.setQuantity(dto.getQuantity());
        return infoSlot;
    }
}
