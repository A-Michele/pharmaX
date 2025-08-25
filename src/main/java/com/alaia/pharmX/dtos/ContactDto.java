package com.alaia.pharmX.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactDto {

	private long id;
    private String email;
    private String phoneNumber;

}
