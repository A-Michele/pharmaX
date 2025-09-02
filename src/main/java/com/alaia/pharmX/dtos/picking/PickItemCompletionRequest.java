package com.alaia.pharmX.dtos.picking;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PickItemCompletionRequest {

	private String pickItemCode;

	@PositiveOrZero
	private int quantityPicked;

	private String slotCode;
	private String reason;

}
