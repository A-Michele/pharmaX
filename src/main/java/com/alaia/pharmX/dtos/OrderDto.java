package com.alaia.pharmX.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

import com.alaia.pharmX.models.State;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private long id;
    private String code;
    private State state;
    private String cf;
    private Set<OrderLineDto> orderLines;
}
