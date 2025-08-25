package com.alaia.pharmX.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotDto {

	private long id;

    @NotBlank
    private String code;

    @PositiveOrZero
    private int volume;

    private String pickingSequence;

}