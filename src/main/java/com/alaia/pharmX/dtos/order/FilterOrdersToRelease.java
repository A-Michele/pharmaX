package com.alaia.pharmX.dtos.order;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterOrdersToRelease {
	private LocalDateTime date;
	private String cf;
}
