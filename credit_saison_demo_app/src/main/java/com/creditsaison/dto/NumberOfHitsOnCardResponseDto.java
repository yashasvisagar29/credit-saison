package com.creditsaison.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class NumberOfHitsOnCardResponseDto extends BaseResponseDto {

	private Integer start;
	private Integer limit;
	private Long size;

}
 