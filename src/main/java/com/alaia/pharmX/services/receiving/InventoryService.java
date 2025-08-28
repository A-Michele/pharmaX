package com.alaia.pharmX.services.receiving;

import java.time.LocalDateTime;
import java.util.List;
import com.alaia.pharmX.dtos.receiving.MovementDto;
import com.alaia.pharmX.dtos.receiving.StockBySlotDto;
import com.alaia.pharmX.dtos.receiving.StockItemDto;

public interface InventoryService {

	List<MovementDto> getMovementsByReceipt(Long receiptId);
	List<StockItemDto> getStock();
	List<StockItemDto> getStockAsOf(LocalDateTime asOf);
	List<MovementDto> getMovementsByNationalCode(String nationalCode);
	StockItemDto getStokOfNationalCode(String nationalCode);
	List<MovementDto> getMovementsByOrder(Long orderId);
	List<StockBySlotDto> getStockBySlot();
}