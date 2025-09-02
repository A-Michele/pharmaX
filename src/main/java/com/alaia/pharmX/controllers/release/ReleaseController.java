package com.alaia.pharmX.controllers.release;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaia.pharmX.dtos.picking.PickListDto;
import com.alaia.pharmX.services.release.ReleaseService;

@RestController
@RequestMapping("/release")
public class ReleaseController {

	@Autowired
	private ReleaseService releaseService;

	@PostMapping
    public ResponseEntity<List<PickListDto>> releaseOrder(@RequestBody List<String> ordersToRelease) {
		List<PickListDto> pickLists = releaseService.releaseOrders(ordersToRelease);
		return new ResponseEntity<>(pickLists, HttpStatus.OK);
    }
}
