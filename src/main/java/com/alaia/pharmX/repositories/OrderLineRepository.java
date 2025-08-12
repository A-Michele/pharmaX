package com.alaia.pharmX.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.OrderLine;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

	void deleteByOrder_Code(String code);

	List<OrderLine> findByOrder_Code(String orderCode);
}
