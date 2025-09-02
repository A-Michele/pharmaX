package com.alaia.pharmX.mappers.releasing;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.picking.PickItemDto;
import com.alaia.pharmX.models.picking.PickItem;

@Component
public class PickItemMapper {

	public PickItemDto toDto(PickItem pickItem) {
        if (pickItem == null) return null;

        PickItemDto dto = new PickItemDto();
        dto.setId(pickItem.getId());
        dto.setCode(pickItem.getCode());
        dto.setState(pickItem.getState());
        dto.setQuantityToPick(pickItem.getQuantityToPick());
        dto.setQuantityPicked(pickItem.getQuantityPicked());
        dto.setSlotsCode(pickItem.getSlotsCode());
        dto.setNationalCode(pickItem.getNationalCode());
        dto.setNameProduct(pickItem.getNameProduct());
        dto.setReason(pickItem.getReason());
        dto.setPickingSequence(pickItem.getPickingSequence());
        dto.setCodeOrder(pickItem.getCodeOrder());
        dto.setLineNumber(pickItem.getLineNumber());

        return dto;
    }

    public PickItem toEntity(PickItemDto dto) {
    	if (dto == null) return null;

    	PickItem pickItem = new PickItem();
    	pickItem.setId(dto.getId());
    	pickItem.setCode(dto.getCode());
    	pickItem.setState(dto.getState());
    	pickItem.setQuantityToPick(dto.getQuantityToPick());
    	pickItem.setQuantityPicked(dto.getQuantityPicked());
    	pickItem.setSlotsCode(dto.getSlotsCode());
    	pickItem.setNationalCode(dto.getNationalCode());
    	pickItem.setNameProduct(dto.getNameProduct());
    	pickItem.setReason(dto.getReason());
    	pickItem.setPickingSequence(dto.getPickingSequence());
    	pickItem.setCodeOrder(dto.getCodeOrder());
    	pickItem.setLineNumber(dto.getLineNumber());

        return pickItem;
    }
}
