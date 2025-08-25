package com.alaia.pharmX.mappers.stock;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.receiving.LotDto;
import com.alaia.pharmX.dtos.stock.StockDto;
import com.alaia.pharmX.models.receiving.Lot;
import com.alaia.pharmX.models.stock.Stock;

@Component
public class StockMapper {

	public StockDto toDto(Stock stock) {
		if (stock == null) return null;
		StockDto dto = new StockDto();
		dto.setId(stock.getId() == 0L ? null : stock.getId());
		dto.setNationalCode(stock.getNationalCode());
		dto.setEffectiveQuantity(stock.getEffectiveQuantity());
		dto.setReservedQuantity(stock.getReservedQuantity());
		dto.setLastModification(stock.getLastModification());
		return dto;
	}

	public Stock toEntity(StockDto dto) {
		if (dto == null) return null;
		Stock stock = new Stock();
		stock.setId(dto.getId() == 0L ? null : dto.getId());
		stock.setNationalCode(dto.getNationalCode());
		stock.setEffectiveQuantity(dto.getEffectiveQuantity());
		stock.setReservedQuantity(dto.getReservedQuantity());
		stock.setLastModification(dto.getLastModification());
		return stock;
	}

}