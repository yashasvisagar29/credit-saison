package com.creditsaison.serviceImpl;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.creditsaison.dto.BaseResponseDto;
import com.creditsaison.dto.CardDetailsDto;
import com.creditsaison.dto.NumberOfHitsOnCardResponseDto;
import com.creditsaison.model.NumberOfHitsOnCardDo;
import com.creditsaison.repository.NumberOfHitsOnCardRepo;
import com.creditsaison.service.NumberOfHitsOnCardService;
import com.creditsaison.util.AppErrorMsgConstants;
import com.creditsaison.util.HelperClass;

@Service
public class NumberOfHitsOnCardServiceImpl implements NumberOfHitsOnCardService {

	@Autowired
	private NumberOfHitsOnCardRepo repo;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public ResponseEntity<?> saveOrUpdate(String cardNumber) {
		try {
			if (!HelperClass.checkString(cardNumber)) {
				Optional<NumberOfHitsOnCardDo> numberOfHitsOnCardDo = repo.findById(cardNumber);
				if (numberOfHitsOnCardDo.isPresent()) {
					int count = numberOfHitsOnCardDo.get().getHitCount() + 1;
					numberOfHitsOnCardDo.get().setHitCount(count);
					return new ResponseEntity<>(repo.save(numberOfHitsOnCardDo.get()), HttpStatus.CREATED);
				} else {
					return new ResponseEntity<>(repo.save(new NumberOfHitsOnCardDo(cardNumber, 1)), HttpStatus.CREATED);
				}
			} else {
				return new ResponseEntity<>(AppErrorMsgConstants.INVALID_INPUT + "Please provide valid input.",
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(AppErrorMsgConstants.EXCEPTION_POST_MSG + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> listAllCardHits(Integer start, Integer limit) {
		try {
			if (start >= 0 && limit > 0) {
				Page<NumberOfHitsOnCardDo> pagedDataList = repo.findAll(PageRequest.of(start, limit));
				if (!pagedDataList.getContent().isEmpty()) {
					NumberOfHitsOnCardResponseDto responseDto = new NumberOfHitsOnCardResponseDto();
					responseDto.setLimit(limit);
					responseDto.setStart(start);
					responseDto.setSize(pagedDataList.getTotalElements());
					responseDto.setSuccess(true);
					responseDto.setPayload(pagedDataList.stream()
							.collect(Collectors.toMap(NumberOfHitsOnCardDo::getCardNumber,
									NumberOfHitsOnCardDo::getHitCount, (oldValue, newValue) -> oldValue,
									LinkedHashMap::new)));
					return new ResponseEntity<>(responseDto, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(AppErrorMsgConstants.DATA_NOT_FOUND, HttpStatus.NO_CONTENT);
				}
			} else {
				return new ResponseEntity<>(
						AppErrorMsgConstants.INVALID_INPUT + "Please provide start and limit value.",
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(AppErrorMsgConstants.EXCEPTION_POST_MSG + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> verifyCardFromBinList(String cardNum) {
		try {
			if (!HelperClass.checkString(cardNum)) {
				ResponseEntity<String> responseFromClient = restTemplate.exchange(
						"https://lookup.binlist.net/" + cardNum, HttpMethod.GET, HelperClass.attachHeadersToRequest(),
						String.class);

				if (responseFromClient.getStatusCode().value() == HttpStatus.OK.value()) {
					ResponseEntity<?> responseFromUpdatingHitOnCard = saveOrUpdate(cardNum);
					if (responseFromUpdatingHitOnCard.getStatusCode().value() == HttpStatus.CREATED.value()) {
						JSONObject jsonObject;
						jsonObject = new JSONObject(responseFromClient.getBody());

						String bankName = null;
						if (jsonObject.has("bank") && jsonObject.getJSONObject("bank").has("name")) {
							bankName = jsonObject.getJSONObject("bank").getString("name");
						}

						return new ResponseEntity<>(
								new BaseResponseDto(true, new CardDetailsDto(jsonObject.getString("scheme"),
										jsonObject.getString("type"), bankName)),
								HttpStatus.OK);

					} else {
						return responseFromUpdatingHitOnCard;
					}
				} else {
					return new ResponseEntity<>(responseFromClient.getBody(), responseFromClient.getStatusCode());
				}
			} else {
				return new ResponseEntity<>(AppErrorMsgConstants.INVALID_INPUT + "Please provide valid input.",
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(AppErrorMsgConstants.EXCEPTION_POST_MSG + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
