package com.alaia.pharmX.services.receiving;

import java.util.List;
import com.alaia.pharmX.dtos.receiving.CreateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.CreateReceiptRequest;
import com.alaia.pharmX.dtos.receiving.PostReceiptResponse;
import com.alaia.pharmX.dtos.receiving.ReceiptDto;
import com.alaia.pharmX.dtos.receiving.SlotDefiniedAtPost;
import com.alaia.pharmX.dtos.receiving.UpdateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.VerifyReceiptLineRequest;

public interface ReceiptService {

    ReceiptDto createDraft(CreateReceiptRequest request);
    ReceiptDto addLine(Long receiptId, CreateReceiptLineRequest line);
    ReceiptDto updateLine(Long receiptId, Long lineId, UpdateReceiptLineRequest line);
    ReceiptDto deleteLineToReceipt(Long receiptId, Long lineId);
    ReceiptDto verifyLine(Long receiptId, Long lineId, VerifyReceiptLineRequest request);
    ReceiptDto completeVerification(Long receiptId);
    PostReceiptResponse postReceipt(Long receiptId, List<SlotDefiniedAtPost> listSlotAtPost);
    ReceiptDto cancel(Long receiptId);
    ReceiptDto getReceiptById(Long receiptId);
    List<ReceiptDto> listReceipt();
    ReceiptDto deleteReceipt(Long receiptId);

}