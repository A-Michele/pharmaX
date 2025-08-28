package com.alaia.pharmX.mappers.receiving;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.receiving.ReceiptDto;
import com.alaia.pharmX.dtos.receiving.ReceiptLineDto;
import com.alaia.pharmX.models.receiving.Receipt;
import com.alaia.pharmX.models.receiving.ReceiptLine;

@Component
public class ReceiptMapper {

	@Autowired
    private ReceiptLineMapper receiptLineMapper;

	public ReceiptDto toDto(Receipt r) {
        if (r == null) return null;

        ReceiptDto dto = new ReceiptDto();
        dto.setId(r.getId());
        dto.setExternalRef(r.getExternalRef());
        dto.setSupplierName(r.getSupplierName());
        dto.setReceivedAt(r.getReceivedAt());
        dto.setLastModification(r.getLastModification());
        dto.setState(r.getState());

        if (r.getLines() != null) {
            Set<ReceiptLineDto> lines = r.getLines().stream()
                    .map(receiptLineMapper::toDto)
                    .collect(Collectors.toSet());
            dto.setLines(lines);

        }

        return dto;
	}

	public Receipt toEntity(ReceiptDto dto) {
		if (dto == null) return null;

		Receipt e = new Receipt();
		if (dto.getId() != null) e.setId(dto.getId()); // entity usa long
		e.setExternalRef(dto.getExternalRef());
		e.setSupplierName(dto.getSupplierName());
		e.setReceivedAt(dto.getReceivedAt());
		e.setLastModification(dto.getLastModification());
		e.setState(dto.getState());

		if (dto.getLines() != null) {
			Set<ReceiptLine> lines = dto.getLines().stream()
					.map(lineDto -> receiptLineMapper.toEntity(lineDto))
					.collect(Collectors.toSet());
			e.setLines(lines);
			e.getLines().forEach(l -> l.setReceipt(e));
		}

		return e;
	}
}