package com.alaia.pharmX.dtos.picking;

import java.time.LocalDateTime;
import java.util.List;

import com.alaia.pharmX.models.picking.PickListStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PickListDto {

	private long id;
	private String code;
	private PickListStatus state;
	private LocalDateTime createdAt;
	private LocalDateTime lastModification;
	private String cf;
    private List<PickItemDto> items;

}