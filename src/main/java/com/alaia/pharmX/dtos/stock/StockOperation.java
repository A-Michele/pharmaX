package com.alaia.pharmX.dtos.stock;

import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.models.receiving.MovementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockOperation {
	String nationalCode;
	String referenceType;
	Long referenceId;
	MovementType type;
	int quantity;
	Slot slot;
}
