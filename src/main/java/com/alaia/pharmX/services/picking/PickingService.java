package com.alaia.pharmX.services.picking;

import java.util.List;
import com.alaia.pharmX.dtos.picking.ItemToPick;

public interface PickingService {

	List<ItemToPick> listItemToPick(String orderCode);

}