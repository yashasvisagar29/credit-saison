package com.creditsaison.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public @Data class NumberOfHitsOnCardDo {

	@Id
	private String cardNumber;
	private Integer hitCount;

}
 