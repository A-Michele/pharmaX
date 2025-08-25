package com.alaia.pharmX.controllers.receiving;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alaia.pharmX.dtos.receiving.CreateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.CreateReceiptRequest;
import com.alaia.pharmX.dtos.receiving.PostReceiptResponse;
import com.alaia.pharmX.dtos.receiving.ReceiptDto;
import com.alaia.pharmX.dtos.receiving.SlotDefiniedAtPost;
import com.alaia.pharmX.dtos.receiving.UpdateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.VerifyReceiptLineRequest;
import com.alaia.pharmX.services.receiving.ReceiptService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/receipt")
public class ReceiptController {

	@Autowired
	private ReceiptService receiptService;

	@PostMapping
    public ResponseEntity<ReceiptDto> create(@Valid @RequestBody CreateReceiptRequest req) {
		ReceiptDto createdReceiptDto = receiptService.createDraft(req);
		return new ResponseEntity<>(createdReceiptDto, HttpStatus.CREATED);
    }

	@GetMapping("/{id}")
	public ResponseEntity<ReceiptDto> get(@PathVariable Long id) {
		ReceiptDto receiptDto = receiptService.getReceiptById(id);
		return new ResponseEntity<>(receiptDto, HttpStatus.OK);
	}

	@GetMapping("/all")
	public ResponseEntity<List<ReceiptDto>> getAllReceipts() {
		List<ReceiptDto> receiptsDto = receiptService.listReceipt();
		return new ResponseEntity<>(receiptsDto, HttpStatus.OK);
	}

	@PostMapping("/{id}/lines")
    public ResponseEntity<ReceiptDto> addLine(@PathVariable Long id, @Valid @RequestBody CreateReceiptLineRequest req) {
		ReceiptDto receiptDto = receiptService.addLine(id, req);
		return new ResponseEntity<>(receiptDto, HttpStatus.CREATED);
    }

	@PatchMapping("/{id}/lines/{lineId}")
	public ResponseEntity<ReceiptDto> updateLine(@PathVariable Long id, @PathVariable Long lineId,
												 @Valid @RequestBody UpdateReceiptLineRequest req) {
		ReceiptDto receiptDto = receiptService.updateLine(id, lineId, req);
		return new ResponseEntity<>(receiptDto, HttpStatus.OK);
	}

	@DeleteMapping("/{id}/lines/{lineId}")
    public ResponseEntity<ReceiptDto> deleteLine(@PathVariable Long id, @PathVariable Long lineId) {
		ReceiptDto receiptDto = receiptService.deleteLineToReceipt(id, lineId);
		return new ResponseEntity<>(receiptDto, HttpStatus.OK);
    }

	@PostMapping("/{id}/lines/{lineId}/verify")
	public ResponseEntity<ReceiptDto> verifyLine(@PathVariable Long id, @PathVariable Long lineId,
												 @Valid @RequestBody VerifyReceiptLineRequest req) {
		ReceiptDto receiptDto = receiptService.verifyLine(id, lineId, req);
		return new ResponseEntity<>(receiptDto, HttpStatus.OK);
	}

	@PostMapping("/{id}/verify/complete")
    public ResponseEntity<ReceiptDto> completeVerification(@PathVariable("id") Long receiptId) {
		ReceiptDto receiptDto = receiptService.completeVerification(receiptId);
		return new ResponseEntity<>(receiptDto, HttpStatus.OK);
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<PostReceiptResponse> post(@PathVariable("id") Long receiptId, @RequestBody List<SlotDefiniedAtPost> slotDefinied) {
    	PostReceiptResponse receipt = receiptService.postReceipt(receiptId, slotDefinied);
        return new ResponseEntity<>(receipt, HttpStatus.OK);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReceiptDto> cancel(@PathVariable("id") Long receiptId) {
    	ReceiptDto receiptDto = receiptService.cancel(receiptId);
    	return new ResponseEntity<>(receiptDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ReceiptDto> deleteReceipt(@PathVariable("id") Long receiptId) {
    	ReceiptDto receiptDto = receiptService.deleteReceipt(receiptId);
        return new ResponseEntity<>(receiptDto, HttpStatus.OK);
    }
}