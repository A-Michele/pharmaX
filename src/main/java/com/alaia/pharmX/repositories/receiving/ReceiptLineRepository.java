package com.alaia.pharmX.repositories.receiving;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.receiving.ReceiptLine;

public interface ReceiptLineRepository extends JpaRepository<ReceiptLine, Long> {

}