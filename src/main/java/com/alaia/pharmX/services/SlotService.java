package com.alaia.pharmX.services;

import java.util.List;

import com.alaia.pharmX.dtos.SlotDto;
import com.alaia.pharmX.dtos.SlotPatchDto;

public interface SlotService {
	SlotDto create(SlotDto dto, Long sectionId);
	SlotDto getById(long id);
	SlotDto getByCode(String code);
	List<SlotDto> getAllSlots();
	SlotDto patchSlot(long id, SlotPatchDto dto);
	SlotDto delete(long id);
	SlotDto move(long slotId, long targetSectionId);
}
