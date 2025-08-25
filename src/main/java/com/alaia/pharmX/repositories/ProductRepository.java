package com.alaia.pharmX.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	boolean existsByNationalCode(String nationalCode);
	Product findByNationalCode(String nationalCode);
	List<Product> findByCategory(String category);

}