package com.alaia.pharmX.dtos;

import com.alaia.pharmX.models.State;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateUpdateRequest {
	@NotNull
	private State newState;
}
