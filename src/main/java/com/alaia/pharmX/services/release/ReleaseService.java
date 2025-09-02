package com.alaia.pharmX.services.release;

import java.util.List;

import com.alaia.pharmX.dtos.picking.PickListDto;

public interface ReleaseService {
	List<PickListDto> releaseOrders(List<String> orders);
}
