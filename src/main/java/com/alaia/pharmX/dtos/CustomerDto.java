package com.alaia.pharmX.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {

    private long id;

    @NotNull
    private String name;

    private String shippingAddress;
    private String billingAddress;

    private ContactDto contacts;

    @NotNull
    @Size(min = 16, max = 16, message = "must be exactly 16 characters")
    private String cf;
}