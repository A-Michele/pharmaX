package com.alaia.pharmX.services.receiving;

import com.alaia.pharmX.dtos.receiving.LotDto;

public interface LotService {

    LotDto getByCode(String lotCode);
}