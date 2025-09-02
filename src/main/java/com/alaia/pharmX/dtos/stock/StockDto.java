package com.alaia.pharmX.dtos.stock;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDto {

	private Long id;

	@NotBlank
	private String nationalCode;

	@Min(0)
	private int effectiveQuantity;

	@Min(0)
	private int reservedQuantity;

	private List<InfoSlotDto> infoSlots;

	private LocalDateTime lastModification;

}