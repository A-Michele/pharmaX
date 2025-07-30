package com.alaia.pharmX.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	boolean existsByCF(String cf);
	Customer findByCF(String cf);
}
