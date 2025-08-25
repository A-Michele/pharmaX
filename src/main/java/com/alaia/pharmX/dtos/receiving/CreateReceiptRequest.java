package com.alaia.pharmX.dtos.receiving;

import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReceiptRequest {

	@NotBlank
	private String externalRef;

	@NotBlank
    private String supplierName;

    @Valid
    private Set<@Valid CreateReceiptLineRequest> lines;

}