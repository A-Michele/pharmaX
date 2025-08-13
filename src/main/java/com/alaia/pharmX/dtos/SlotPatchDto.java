package com.alaia.pharmX.dtos;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotPatchDto {
	@PositiveOrZero
	private Integer volume;

	private String pickingSequence;
}
