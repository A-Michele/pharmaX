package com.alaia.pharmX.repositories.picking;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.picking.PickItem;

public interface PickItemRepository extends JpaRepository<PickItem, Long> {

	PickItem findByCode(String pickItemCode);

	PickItem findByLineNumber(String lineNumber);

}