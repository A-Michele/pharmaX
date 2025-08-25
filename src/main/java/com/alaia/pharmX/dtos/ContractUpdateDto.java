package com.alaia.pharmX.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractUpdateDto {

    private long id;
    private String email;
    private String phoneNumber;

    @NotNull
    private Boolean deleteMail;

    @NotNull
    private Boolean deletePhoneNumber;

}