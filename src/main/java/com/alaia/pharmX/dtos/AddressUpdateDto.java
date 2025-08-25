package com.alaia.pharmX.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressUpdateDto {

	private String shippingAddress;
    private String billingAddress;

    @NotNull
    private Boolean deleteShippingAddress;

    @NotNull
    private Boolean deleteBillingAddress;

}
