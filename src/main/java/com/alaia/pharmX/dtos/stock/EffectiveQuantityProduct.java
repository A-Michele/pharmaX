package com.alaia.pharmX.dtos.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EffectiveQuantityProduct {

	String nationalCode;
	private int effectiveQuantity;

}
