package com.alaia.pharmX.services.picking;

import java.util.List;

import com.alaia.pharmX.dtos.picking.ItemToPick;
import com.alaia.pharmX.dtos.picking.PickItemDto;
import com.alaia.pharmX.dtos.picking.PickListDto;
import com.alaia.pharmX.dtos.picking.PickItemCompletionRequest;

public interface PickingService {

	PickItemDto	completePickItem( PickItemCompletionRequest request );
	ItemToPick getNextItemToPick(List<String> pickListCodes);
	PickItemDto getPickItemDtoByCode(String pickItemCode);
	PickListDto getPickListDtoByCode(String pickListCode);
	List<PickItemDto> getPickItemsByPickListCode(String pickListCode);
}