package com.alaia.pharmX.mappers;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.SlotDto;
import com.alaia.pharmX.models.Slot;

@Component
public class SlotMapper {

    public SlotDto toDto(Slot slot) {
        if (slot == null) return null;

        SlotDto dto = new SlotDto();
        dto.setId(slot.getId());
        dto.setCode(slot.getCode());
        dto.setVolume(slot.getVolume());
        dto.setPickingSequence(slot.getPickingSequence());

        //Non settiamo SectionDto per evitare ricorsione
        return dto;
    }

    public Slot toEntity(SlotDto dto) {
        if (dto == null) return null;

        Slot slot = new Slot();
        slot.setId(dto.getId());
        slot.setCode(dto.getCode());
        slot.setVolume(dto.getVolume());
        slot.setPickingSequence(dto.getPickingSequence());

        //Section verrà impostato altrove, quando colleghiamo i Slot alla Section
        return slot;
    }
}
