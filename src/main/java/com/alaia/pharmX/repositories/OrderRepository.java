package com.alaia.pharmX.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	boolean existsByCode(String code);

	Order findByCode(String code);
}

