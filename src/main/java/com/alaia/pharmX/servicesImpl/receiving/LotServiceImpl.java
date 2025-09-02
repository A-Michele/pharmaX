package com.alaia.pharmX.servicesImpl.receiving;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alaia.pharmX.dtos.receiving.LotDto;
import com.alaia.pharmX.exceptions.servicesImpl.LotNotFoundException;
import com.alaia.pharmX.mappers.receiving.LotMapper;
import com.alaia.pharmX.models.receiving.Lot;
import com.alaia.pharmX.repositories.receiving.LotRepository;
import com.alaia.pharmX.services.receiving.LotService;

@Service
public class LotServiceImpl implements LotService {

	@Autowired
	private LotRepository lotRepository;

	@Autowired
	private LotMapper lotMapper;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public LotDto getByCode(String lotCode) {
		Lot lot = lotRepository.findByLotCode(lotCode);
		if(lot == null ) {
			throw new LotNotFoundException("Lot not found with code: " + lotCode);
		}
		return lotMapper.toDto(lot);
	}
}