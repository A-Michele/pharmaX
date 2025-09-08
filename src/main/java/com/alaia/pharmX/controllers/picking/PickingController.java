package com.alaia.pharmX.controllers.picking;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.picking.ItemToPick;
import com.alaia.pharmX.dtos.picking.PickItemDto;
import com.alaia.pharmX.dtos.picking.PickListDto;
import com.alaia.pharmX.dtos.picking.PickItemCompletionRequest;
import com.alaia.pharmX.services.picking.PickingService;

@RestController
@RequestMapping("/picking")
public class PickingController {

	@Autowired
    private PickingService pickingService;

	@GetMapping("/next-item-to-pick")
	public ResponseEntity<ItemToPick> getItemToPick(@RequestBody List<String> pickListCodes) {
		ItemToPick item = pickingService.getNextItemToPick(pickListCodes);
		return new ResponseEntity<>(item, HttpStatus.OK);
	}

	@PatchMapping("/pick-item")
	public ResponseEntity<PickItemDto> pickItem(@RequestBody  PickItemCompletionRequest uqPickedItem) {
		PickItemDto item = pickingService.completePickItem(uqPickedItem);
		return new ResponseEntity<>(item, HttpStatus.OK);
	}

	@GetMapping("/pick-item/{pickItemCode}")
	public ResponseEntity<PickItemDto> getPickItem(@PathVariable String pickItemCode) {
		PickItemDto item = pickingService.getPickItemDtoByCode(pickItemCode);
		return new ResponseEntity<>(item, HttpStatus.OK);
	}

	@GetMapping("/pick-list/{pickListCode}")
	public ResponseEntity<PickListDto> getPickList(@PathVariable String pickListCode) {
		PickListDto item = pickingService.getPickListDtoByCode(pickListCode);
		return new ResponseEntity<>(item, HttpStatus.OK);
	}

	@GetMapping("/{pickListCode}/pickList-items")
	public ResponseEntity<List<PickItemDto>> getItemsToPickLists(@PathVariable String pickListCode) {
		List<PickItemDto> items = pickingService.getPickItemsByPickListCode(pickListCode);
		return new ResponseEntity<>(items, HttpStatus.OK);
	}

	@GetMapping("/orders-released")
    public ResponseEntity<List<String>> getRelesedOrders() {
		List<String> list = pickingService.getOrderReleased();
		return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
