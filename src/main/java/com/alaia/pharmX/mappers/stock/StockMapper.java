package com.alaia.pharmX.mappers.stock;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.stock.InfoSlotDto;
import com.alaia.pharmX.dtos.stock.StockDto;
import com.alaia.pharmX.models.stock.InfoSlot;
import com.alaia.pharmX.models.stock.Stock;

@Component
public class StockMapper {

	@Autowired
    private InfoSlotMapper infoSlotMapper;

	public StockDto toDto(Stock stock) {
		if (stock == null) return null;
		StockDto dto = new StockDto();
		dto.setId(stock.getId());
		dto.setNationalCode(stock.getNationalCode());
		dto.setEffectiveQuantity(stock.getEffectiveQuantity());
		dto.setReservedQuantity(stock.getReservedQuantity());
		dto.setLastModification(stock.getLastModification());
		if (stock.getInfoSlots() != null) {
            List<InfoSlotDto> slots = stock.getInfoSlots().stream()
                .map(infoSlotMapper::toDto)
                .toList();
            dto.setInfoSlots(slots);
        }
		return dto;
	}

	public Stock toEntity(StockDto dto) {
		if (dto == null) return null;
		Stock stock = new Stock();
		stock.setId(dto.getId());
		stock.setNationalCode(dto.getNationalCode());
		stock.setEffectiveQuantity(dto.getEffectiveQuantity());
		stock.setReservedQuantity(dto.getReservedQuantity());
		stock.setLastModification(dto.getLastModification());

		if (dto.getInfoSlots() != null) {
			List<InfoSlot> slots = new ArrayList<>();

			for (InfoSlotDto slotDto : dto.getInfoSlots() ){
				InfoSlot slot = infoSlotMapper.toEntity(slotDto);
				slot.setStock(stock);
				slots.add(slot);
			}

			stock.setInfoSlots(slots);;
		}

		return stock;
	}

}