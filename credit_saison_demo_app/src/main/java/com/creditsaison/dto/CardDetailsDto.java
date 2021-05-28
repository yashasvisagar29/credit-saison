package com.creditsaison.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public @Data class CardDetailsDto {

	private String scheme;
	private String type;
	private String bank;

}
 