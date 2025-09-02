package com.alaia.pharmX.repositories.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alaia.pharmX.dtos.order.FilterOrdersToRelease;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.State;

public interface OrderRepository extends JpaRepository<Order, Long> {

	boolean existsByCode(String code);
	Order findByCode(String code);
	List<Order> findByCfAndState(String cf, State desiredState);
	List<Order> findByCf(String cf);

	@Query("SELECT o FROM Order o WHERE (o.cf = :#{#filter.cf} OR o.date BETWEEN :#{#filter.date.minusDays(5)} AND :#{#filter.date.plusDays(5)}) AND o.state = com.alaia.pharmX.models.order.State.OPEN")
	List<Order> findOrdersByCfOrDateRange(@Param("filter") FilterOrdersToRelease filter);

}