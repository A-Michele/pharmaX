package com.alaia.pharmX.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}

