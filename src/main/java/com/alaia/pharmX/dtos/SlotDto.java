package com.alaia.pharmX.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotDto {
    private long id;
    private String code;
    private int volume;
    private String pickingSequence;

    //Non includiamo SectionDto per evitare loop
}