package com.alaia.pharmX.dtos.receiving;

import java.util.List;

import com.alaia.pharmX.models.receiving.ReceiptLineStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyReceiptLineRequest {

	@PositiveOrZero
	private Integer qtyReceived;

	@NotNull
	private ReceiptLineStatus status;

	private String reason;

	private String putwaySlotCode; //si può definire al receiving o dopo con la post

	@Valid
	private List<@Valid CreateLotRequest> lots; // se null → lotto unico inferito

}