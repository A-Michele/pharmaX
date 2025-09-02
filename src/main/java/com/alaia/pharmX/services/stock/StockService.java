package com.alaia.pharmX.services.stock;

import com.alaia.pharmX.dtos.stock.EffectiveQuantityProduct;
import com.alaia.pharmX.dtos.stock.ReservedQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockDto;
import com.alaia.pharmX.dtos.stock.StockOperation;
import java.util.List;
import com.alaia.pharmX.dtos.picking.PickItemCompletionRequest;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;

public interface StockService {

	List<StockDto> getAllStock();

	EffectiveQuantityProduct getEffectiveQuantity(String nationalCode);
	ReservedQuantityProduct getReservedQuantity(String nationalCode);
	AvailableQuantityProduct getAvailableQuantity(String nationalCode);

	StockDto createStock(StockDto stockDto);

	StockDto reserveQuantity(StockOperation opreation);
	StockDto unReserveQuantity(StockOperation opreation, PickItemCompletionRequest uqPickedItem);
	StockDto unReserveQuantityOnDeleteOrCanceled(StockOperation operation);
	StockDto onReceiptOpration(StockOperation operation);

	StockDto updateEffectiveQuantity(EffectiveQuantityProduct effectiveQuantityProduct);

}