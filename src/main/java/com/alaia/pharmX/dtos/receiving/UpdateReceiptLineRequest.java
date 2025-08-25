package com.alaia.pharmX.dtos.receiving;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReceiptLineRequest {

	@PositiveOrZero
	private Integer qtyExpected;

}