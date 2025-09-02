package com.alaia.pharmX.servicesImpl.picking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import com.alaia.pharmX.dtos.picking.ItemToPick;
import com.alaia.pharmX.dtos.picking.PickItemDto;
import com.alaia.pharmX.dtos.picking.PickListDto;
import com.alaia.pharmX.dtos.picking.PickItemCompletionRequest;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.NoPickItemAvailableException;
import com.alaia.pharmX.exceptions.servicesImpl.PickItemNotFound;
import com.alaia.pharmX.exceptions.servicesImpl.PickListNotFoundException;
import com.alaia.pharmX.mappers.releasing.PickItemMapper;
import com.alaia.pharmX.mappers.releasing.PickListMapper;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.models.order.LineOrderType;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.picking.PickItem;
import com.alaia.pharmX.models.picking.PickList;
import com.alaia.pharmX.models.picking.PickListItemStatus;
import com.alaia.pharmX.models.picking.PickListStatus;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.repositories.picking.PickItemRepository;
import com.alaia.pharmX.repositories.picking.PickListRepository;
import com.alaia.pharmX.services.picking.PickingService;
import com.alaia.pharmX.services.stock.StockService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PickingServiceImpl implements PickingService{

	@Autowired
	private PickListRepository pickListRepository;

	@Autowired
	private PickItemRepository pickItemRepository;

	@Autowired
	private OrderLineRepository orderLineRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private StockService stockService;

	@Autowired
	private PickItemMapper pickItemMapper;

	@Autowired
	private PickListMapper pickListMapper;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public PickItemDto completePickItem(PickItemCompletionRequest request) {
        PickItem pickItem = validatePickItem(request);

        setPickItemValues(pickItem, request);
        OrderLine orderLine = setOrderLineValues(pickItem);
        Order order = setOrderValues(pickItem);
        StockOperation operation = buildStockOperation(pickItem.getNationalCode(), request, pickItem.getPickList().getId());

        stockService.unReserveQuantity(operation, request);

        saveEntities(pickItem, order, orderLine);

        return pickItemMapper.toDto(pickItem);
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ItemToPick getNextItemToPick(List<String> pickListCodes) {
        List<PickList> pickLists = validatePickLists(pickListCodes);

        PickItem firstItem = collectAndSortPickItems(pickLists);

        return mapToItemToPick(firstItem);
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public PickItemDto getPickItemDtoByCode(String pickItemCode) {
		PickItem pick = pickItemRepository.findByCode(pickItemCode);
		if(pick == null) throw new PickItemNotFound("PickItem: " + pickItemCode + " not found");
		return pickItemMapper.toDto(pick);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public PickListDto getPickListDtoByCode(String pickListCode) {
		PickList list = pickListRepository.findByCode(pickListCode);
		if(list == null) throw new PickListNotFoundException("PickList: " + pickListCode + " not found");
		return pickListMapper.toDto(list);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<PickItemDto> getPickItemsByPickListCode(String pickListCode){
		PickListDto list = this.getPickListDtoByCode(pickListCode);
		return list.getItems();
	}

	//----> HELEPERS FOR UPDATE QUANTITY PICKED <-----

	private PickItem validatePickItem(PickItemCompletionRequest uqPickedItem) {
        PickItem pickItem = pickItemRepository.findByCode(uqPickedItem.getPickItemCode());
        if (pickItem == null) {
            throw new PickItemNotFound("PickItem: " + uqPickedItem.getPickItemCode() + " not found");
        }

        if (pickItem.getState().equals(PickListItemStatus.PICKED)) {
            throw new IllegalArgumentException("Item: " + uqPickedItem.getPickItemCode() + " has already been picked");
        }

        if (uqPickedItem.getQuantityPicked() > pickItem.getQuantityToPick()) {
            throw new IllegalArgumentException("Picked: " + uqPickedItem.getQuantityPicked() +
                    ", too much of the quantity to pick: " + pickItem.getQuantityToPick());
        }

        if (uqPickedItem.getQuantityPicked() < pickItem.getQuantityToPick() && uqPickedItem.getReason() == null) {
            throw new IllegalArgumentException("Reason cannot be null");
        }

        if (!isSlotPresent(pickItem.getSlotsCode(), uqPickedItem.getSlotCode())) {
            throw new IllegalArgumentException(uqPickedItem.getSlotCode() + " is not correct slot to pick the item");
        }

        return pickItem;
    }

    private void setPickItemValues(PickItem pickItem, PickItemCompletionRequest uqPickedItem) {
        if (pickItem.getQuantityPicked() == 0) {
            pickItem.setQuantityPicked(uqPickedItem.getQuantityPicked());
        }

        if (uqPickedItem.getReason() != null) {
            pickItem.setReason(uqPickedItem.getReason());
        }

        pickItem.setState(PickListItemStatus.PICKED);

        if (verifiedAllItemArePicked(pickItem.getPickList())) {
            pickItem.getPickList().setState(PickListStatus.PICKED);
        }
    }

    private OrderLine setOrderLineValues(PickItem pickItem) {
        OrderLine orderLine = orderLineRepository.findByLineNumber(pickItem.getLineNumber());
        orderLine.setType(LineOrderType.PICKED);
        return orderLine;
    }

    private Order setOrderValues(PickItem pickItem) {
        Order order = orderRepository.findByCode(pickItem.getCodeOrder());
        order.setState(State.PICKING);
        return order;
    }

    private void saveEntities(PickItem pickItem, Order order, OrderLine orderLine) {
        pickListRepository.save(pickItem.getPickList());
        orderRepository.save(order);
        orderLineRepository.save(orderLine);
    }

    private StockOperation buildStockOperation(String nationalCode, PickItemCompletionRequest item, long referenceId) {
        StockOperation operation = new StockOperation();
        operation.setNationalCode(nationalCode);
        operation.setQuantity(item.getQuantityPicked());

        Optional<Slot> slot = slotRepository.findByCode(item.getSlotCode());
        operation.setSlot(slot.get());

        operation.setType(MovementType.PICKING);
        operation.setReferenceId(referenceId);
        operation.setReferenceType("PICKING");

        return operation;
    }

    private boolean verifiedAllItemArePicked(PickList pickList) {
        for (PickItem item : pickList.getItems()) {
            if (!item.getState().equals(PickListItemStatus.PICKED)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSlotPresent(String slotsCode, String slotToCheck) {
        if (slotsCode == null || slotToCheck == null) {
            return false;
        }

        String[] slots = slotsCode.split(" \\| ");
        return Arrays.asList(slots).contains(slotToCheck);
    }

    //----> HELEPERS FOR GET NEXT ITEM TO PICK <-----
    private List<PickList> validatePickLists(List<String> pickListCodes) {
    	if (pickListCodes == null || pickListCodes.isEmpty()) {
    		throw new IllegalArgumentException("The PickList code list cannot be empty");
    	}

    	List<PickList> pickLists = pickListRepository.findByCodeIn(pickListCodes);
    	if (pickLists.size() != pickListCodes.size()) {
    		throw new PickListNotFoundException("One or more PickLists not found");
    	}

    	return pickLists;
    }

    private PickItem collectAndSortPickItems(List<PickList> pickLists) {
    	List<PickItem> openPickItems = new ArrayList<>();
    	for (PickList pickList : pickLists) {
    		if (pickList.getItems() != null) {
    			openPickItems.addAll(pickList.getItems().stream()
    					.filter(item -> item.getState() == PickListItemStatus.OPEN)
    					.toList());
    		}
    	}

    	if (openPickItems.isEmpty()) {
    		throw new NoPickItemAvailableException("No PickItems in OPEN status available in the selected PickLists");
    	}

    	openPickItems.sort(Comparator.comparing(PickItem::getPickingSequence)
    			.thenComparing(PickItem::getNameProduct));

    	return openPickItems.get(0);
    }

    private ItemToPick mapToItemToPick(PickItem firstItem) {
    	ItemToPick dto = new ItemToPick();
    	dto.setPickListCode(firstItem.getPickList().getCode());
    	dto.setPickItemCode(firstItem.getCode());
    	dto.setSerialNumber(firstItem.getNationalCode());
    	dto.setNameProduct(firstItem.getNameProduct());
    	dto.setSlotsCode(firstItem.getSlotsCode());
    	dto.setQuantityToPicked(firstItem.getQuantityToPick());
    	dto.setOrderCode(firstItem.getCodeOrder());
    	dto.setPickingSequence(firstItem.getPickingSequence());
    	return dto;
    }

}
