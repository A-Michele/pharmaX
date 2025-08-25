package com.alaia.pharmX.dtos.receiving;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReceiptLineRequest {

	@NotBlank
	private String nationalCode;

	@Positive
    private Integer qtyExpected;

}