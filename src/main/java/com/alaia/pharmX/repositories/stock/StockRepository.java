package com.alaia.pharmX.repositories.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.stock.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

	Stock findByNationalCode(String nationalCode);
	boolean existsByNationalCode(String nationalCode);

}
