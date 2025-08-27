package com.alaia.pharmX.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

import com.alaia.pharmX.models.State;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

    private long id;

	@NotBlank
    private String code;

    private State state;

	@NotBlank
    private String cf;
    private LocalDateTime date;

    @Valid
    @NotEmpty
    private Set<@Valid OrderLineDto> orderLines;

}