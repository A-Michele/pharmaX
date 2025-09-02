package com.alaia.pharmX.repositories.picking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.picking.PickList;

public interface PickListRepository extends JpaRepository<PickList, Long> {

	List<PickList> findByCodeIn(List<String> pickListCodes);

	PickList findByCode(String pickListCode);

}
