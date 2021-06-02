package com.creditsaison.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.creditsaison.service.NumberOfHitsOnCardService;
import com.creditsaison.util.HelperClass;

@RestController
@CrossOrigin
@RequestMapping("/card-scheme")
public class CardSchemeController {

	@Autowired
	private NumberOfHitsOnCardService services;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/lookup/{cardNum}")
	public ResponseEntity<?> lookForCardInBinlist(@PathVariable String cardNum) {
		ResponseEntity<String> responseFromClient = restTemplate.exchange("https://lookup.binlist.net/" + cardNum,
				HttpMethod.GET, HelperClass.attachHeadersToRequest(), String.class);
		return (responseFromClient.getStatusCode().value() == HttpStatus.OK.value()) ? services.saveOrUpdate(cardNum)
				: responseFromClient;
	}

	@GetMapping("/verify/{cardNum}")
	public ResponseEntity<?> verifyCardDetails(@PathVariable String cardNum) {

		return services.verifyCardFromBinList(cardNum);
	}

	@GetMapping("/stats")
	public ResponseEntity<?> getAllEmployees(@RequestParam(defaultValue = "0") Integer start,
			@RequestParam(defaultValue = "10") Integer limit) {

		return services.listAllCardHits(start, limit);
	}

}