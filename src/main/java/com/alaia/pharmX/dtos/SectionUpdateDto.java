package com.alaia.pharmX.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionUpdateDto {

	@NotBlank
	private String name;

	@NotBlank
	private String category;

}