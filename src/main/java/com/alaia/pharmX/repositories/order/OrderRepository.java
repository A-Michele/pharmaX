package com.alaia.pharmX.repositories.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.State;

public interface OrderRepository extends JpaRepository<Order, Long> {

	boolean existsByCode(String code);
	Order findByCode(String code);
	List<Order> findByCfAndState(String cf, State desiredState);
	List<Order> findByCf(String cf);

}