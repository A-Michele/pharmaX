package com.alaia.pharmX.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.OrderLine;

public interface OrderLineRepository extends JpaRepository<OrderLine, Integer> {
}
