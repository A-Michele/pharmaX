package com.alaia.pharmX.mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.SectionDto;
import com.alaia.pharmX.dtos.SlotDto;
import com.alaia.pharmX.models.Section;
import com.alaia.pharmX.models.Slot;

@Component
public class SectionMapper {

	@Autowired
    private SlotMapper slotMapper;

	public SectionDto toDto(Section section) {
        if (section == null) return null;

        SectionDto dto = new SectionDto();
        dto.setId(section.getId());
        dto.setCode(section.getCode());
        dto.setName(section.getName());
        dto.setCategory(section.getCategory());

        if (section.getSlots() != null) {
            Set<SlotDto> slotDtos = section.getSlots().stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toSet());

            dto.setSlots(slotDtos);
        }

        return dto;
    }

	public Section toEntity(SectionDto dto) {
        if (dto == null) return null;

        Section section = new Section();
        section.setId(dto.getId());
        section.setCode(dto.getCode());
        section.setName(dto.getName());
        section.setCategory(dto.getCategory());

        if (dto.getSlots() != null) {
            Set<Slot> slots = new HashSet<>();

            for (SlotDto slotDto : dto.getSlots()) {
                Slot slot = slotMapper.toEntity(slotDto);

                //Settiamo il riferimento a Section su ogni Slot
                slot.setSection(section);

                slots.add(slot);
            }

            section.setSlots(slots);
        }

        return section;
    }
}
