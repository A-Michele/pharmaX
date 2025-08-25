package com.alaia.pharmX.mappers.receiving;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.receiving.LotDto;
import com.alaia.pharmX.dtos.receiving.ReceiptLineDto;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.models.receiving.Lot;
import com.alaia.pharmX.models.receiving.ReceiptLine;

@Component
public class ReceiptLineMapper {

	@Autowired
    private LotMapper lotMapper;

    public ReceiptLineDto toDto(ReceiptLine rl) {
        if (rl == null) return null;

        ReceiptLineDto dto = new ReceiptLineDto();
        dto.setId(rl.getId() == 0L ? null : rl.getId());
        dto.setNationalCode(rl.getNationalCode());
        dto.setQtyExpected(rl.getQtyExpected());
        dto.setQtyReceived(rl.getQtyReceived());
        dto.setStatus(rl.getStatus());
        dto.setReason(rl.getReason());
        dto.setPutawaySlotCode(rl.getPutawaySlot() != null ? rl.getPutawaySlot().getCode() : null);

        if (rl.getLots() != null) {
            Set<LotDto> lots = rl.getLots().stream()
                    .map(lotMapper::toDto)
                    .collect(Collectors.toSet());
            dto.setLots(lots);
        }

        return dto;
    }

    public ReceiptLine toEntity(ReceiptLineDto dto) {
        if (dto == null) return null;

        ReceiptLine e = new ReceiptLine();
        if (dto.getId() != null) e.setId(dto.getId());
        e.setNationalCode(dto.getNationalCode());
        e.setQtyExpected(dto.getQtyExpected());
        e.setQtyReceived(dto.getQtyReceived());
        e.setStatus(dto.getStatus());
        e.setReason(dto.getReason());

        Slot slot = new Slot();
        slot.setCode(dto.getPutawaySlotCode());
        e.setPutawaySlot(slot);

        if (dto.getLots() != null) {
        	Set<Lot> lots = dto.getLots().stream()
        			.map(lotMapper::toEntity)
        			.collect(Collectors.toSet());
        	e.setLots(lots);
        	e.getLots().forEach(l -> l.setReceiptLine(e));
        }

        return e;
    }
}