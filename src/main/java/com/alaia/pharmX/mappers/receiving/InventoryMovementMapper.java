package com.alaia.pharmX.mappers.receiving;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.receiving.MovementDto;
import com.alaia.pharmX.models.receiving.InventoryMovement;
import com.alaia.pharmX.models.receiving.MovementType;

@Component
public class InventoryMovementMapper {

    public MovementDto toDto(InventoryMovement m) {
        if (m == null) return null;
        MovementDto dto = new MovementDto();
        dto.setId(m.getId());
        dto.setNationalCode(m.getNationalCode());
        dto.setQuantity(m.getQuantity());
        dto.setType(m.getType() != null ? m.getType().name() : null);
        dto.setReferenceType(m.getReferenceType());
        dto.setReferenceId(m.getReferenceId());
        dto.setSlotCode(m.getSlot() != null ? m.getSlot().getCode() : null);
        dto.setTimestamp(m.getTimestamp());
        return dto;
    }

    public InventoryMovement toEntity(MovementDto dto) {
        if (dto == null) return null;
        InventoryMovement e = new InventoryMovement();
        if (dto.getId() != null) e.setId(dto.getId()); // attenzione: IDENTITY; settalo solo in caso di update
        e.setNationalCode(dto.getNationalCode());
        e.setQuantity(dto.getQuantity());
        if (dto.getType() != null && !dto.getType().isBlank()) {
            e.setType(MovementType.valueOf(dto.getType().trim().toUpperCase()));
        } else {
            e.setType(null);
        }
        e.setReferenceType(dto.getReferenceType());
        e.setReferenceId(dto.getReferenceId());
        e.setTimestamp(dto.getTimestamp());
        return e;
    }
}