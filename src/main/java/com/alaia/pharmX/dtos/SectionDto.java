package com.alaia.pharmX.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionDto {

    private int id;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @Valid
    private Set<@Valid SlotDto> slots;
}