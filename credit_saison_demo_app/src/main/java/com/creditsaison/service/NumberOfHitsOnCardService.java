package com.creditsaison.service;

import org.springframework.http.ResponseEntity;

public interface NumberOfHitsOnCardService {

	ResponseEntity<?> saveOrUpdate(String cardNumber);

	ResponseEntity<?> listAllCardHits(Integer start, Integer limit);
	
	ResponseEntity<?> verifyCardFromBinList(String cardNum);

}
 