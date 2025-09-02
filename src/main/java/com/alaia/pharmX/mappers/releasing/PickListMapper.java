package com.alaia.pharmX.mappers.releasing;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alaia.pharmX.dtos.picking.PickItemDto;
import com.alaia.pharmX.dtos.picking.PickListDto;
import com.alaia.pharmX.models.picking.PickItem;
import com.alaia.pharmX.models.picking.PickList;

@Component
public class PickListMapper {

	@Autowired
    private PickItemMapper pickItemMapper;

	public PickListDto toDto(PickList pickList) {
        if (pickList == null) return null;

        PickListDto dto = new PickListDto();
        dto.setId(pickList.getId());
        dto.setCode(pickList.getCode());
        dto.setState(pickList.getState());
        dto.setCreatedAt(pickList.getCreatedAt());
        dto.setLastModification(pickList.getLastModification());
        dto.setCf(pickList.getCf());

        if (pickList.getItems() != null) {
            List<PickItemDto> items = pickList.getItems().stream()
                .map(pickItemMapper::toDto)
                .toList();
            dto.setItems(items);
        }

        return dto;
    }

	public PickList toEntity(PickListDto dto) {
		if (dto == null) return null;

		PickList pickList = new PickList();
		pickList.setId(dto.getId());
		pickList.setCode(dto.getCode());
		pickList.setState(dto.getState());
		pickList.setCreatedAt(dto.getCreatedAt());
		pickList.setLastModification(dto.getLastModification());
		pickList.setCf(dto.getCf());

		if (dto.getItems() != null) {
			List<PickItem> items = new ArrayList<>();

			for (PickItemDto itemDto : dto.getItems()) {

				PickItem item = pickItemMapper.toEntity(itemDto);
				item.setPickList(pickList);
				items.add(item);
			}

			pickList.setItems(items);
		}

		return pickList;
	}


}
