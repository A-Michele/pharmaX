package com.alaia.pharmX.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionDto {
    private int id;
    private String code;
    private String name;
    private String category;
    private Set<SlotDto> slots;
}
