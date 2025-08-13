package com.alaia.pharmX.services;

import java.util.List;

import com.alaia.pharmX.dtos.SectionDto;
import com.alaia.pharmX.dtos.SectionUpdateDto;

public interface SectionService {
	SectionDto create(SectionDto dto);
    SectionDto getById(long id);
    SectionDto getByCode(String code);
    List<SectionDto> getAllSection();
    SectionDto updateNameAndCategory(long id, SectionUpdateDto dto);
    SectionDto delete(long id);

    SectionDto addExistingSlot(long sectionId, long slotId);
    SectionDto removeSlot(long sectionId, long slotId);
}
