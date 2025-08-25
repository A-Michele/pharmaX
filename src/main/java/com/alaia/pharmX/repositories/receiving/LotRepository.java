package com.alaia.pharmX.repositories.receiving;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.receiving.Lot;

public interface LotRepository extends JpaRepository<Lot, Long> {

	Lot findByLotCode(String lotCode);
	boolean existsByLotCode(String lotCode);

}