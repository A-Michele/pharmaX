package com.alaia.pharmX.services.stock;

import com.alaia.pharmX.dtos.stock.EffectiveQuantityProduct;
import com.alaia.pharmX.dtos.stock.ReservedQuantityProduct;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;

public interface StockService {

	EffectiveQuantityProduct getEffectiveQuantity(String nationalCode);
	ReservedQuantityProduct getReservedQuantity(String nationalCode);
	AvailableQuantityProduct getAvailableQuantity(String nationalCode);

	void reservedQuantity(String nationalCode, int quantity);
	void unReservedQuantity(String nationalCode, int quantity);

}