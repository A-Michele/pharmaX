package com.alaia.pharmX.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
