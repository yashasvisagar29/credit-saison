package com.creditsaison.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.creditsaison.dto.BaseResponseDto;
import com.creditsaison.dto.CardDetailsDto;
import com.creditsaison.service.NumberOfHitsOnCardService;
import com.creditsaison.util.AppErrorMsgConstants;
import com.creditsaison.util.HelperClass;

@RestController
@RequestMapping("/card-scheme")
public class CardSchemeController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired 
	private NumberOfHitsOnCardService services;

	@GetMapping("/lookup/{cardNum}")
	public ResponseEntity<?> lookForCardInBinlist(@PathVariable String cardNum) {
		ResponseEntity<String> responseFromClient = restTemplate.exchange("https://lookup.binlist.net/" + cardNum,
				HttpMethod.GET, HelperClass.attachHeadersToRequest(), String.class);
		return (responseFromClient.getStatusCode().value() == HttpStatus.OK.value()) ? services.saveOrUpdate(cardNum)
				: responseFromClient;
	}

	@GetMapping("/verify/{cardNum}")
	public ResponseEntity<?> verifyCardDetails(@PathVariable String cardNum) {
		ResponseEntity<String> responseFromClient = restTemplate.exchange("https://lookup.binlist.net/" + cardNum,
				HttpMethod.GET, HelperClass.attachHeadersToRequest(), String.class);

		if (responseFromClient.getStatusCode().value() == HttpStatus.OK.value()) {
			ResponseEntity<?> responseFromUpdatingHitOnCard = services.saveOrUpdate(cardNum);
			if (responseFromUpdatingHitOnCard.getStatusCode().value() == HttpStatus.CREATED.value()) {
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(responseFromClient.getBody());

					String bankName = null;
					if (jsonObject.has("bank") && jsonObject.getJSONObject("bank").has("name")) {
						bankName = jsonObject.getJSONObject("bank").getString("name");
					}

					return new ResponseEntity<>(new BaseResponseDto(true,
							new CardDetailsDto(jsonObject.getString("scheme"), jsonObject.getString("type"), bankName)),
							HttpStatus.OK);

				} catch (Exception e) {
					return new ResponseEntity<>(AppErrorMsgConstants.EXCEPTION_POST_MSG + e.getMessage(),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				return responseFromUpdatingHitOnCard;
			}
		} else {
			return responseFromClient;
		}

	}

	@GetMapping("/stats")
	public ResponseEntity<?> getAllEmployees(@RequestParam(defaultValue = "0") Integer start,
			@RequestParam(defaultValue = "10") Integer limit) {

		return services.listAllCardHits(start, limit);
	}

}