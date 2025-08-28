package com.alaia.pharmX.repositories.receiving;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaia.pharmX.models.receiving.Receipt;
import com.alaia.pharmX.models.receiving.ReceiptLine;

public interface ReceiptLineRepository extends JpaRepository<ReceiptLine, Long> {

	Optional<ReceiptLine> findLineByNationalCodeAndReceipt(String nationalCode, Receipt receipt);

}