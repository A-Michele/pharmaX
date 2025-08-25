package com.alaia.pharmX.repositories.receiving;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.receiving.Receipt;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

	Optional<Receipt> findWithLinesById(Long receiptId);

}