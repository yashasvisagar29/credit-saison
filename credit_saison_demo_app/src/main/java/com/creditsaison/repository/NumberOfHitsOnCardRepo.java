package com.creditsaison.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.creditsaison.model.NumberOfHitsOnCardDo;

@Repository
public interface NumberOfHitsOnCardRepo extends PagingAndSortingRepository<NumberOfHitsOnCardDo, String> {
	

}
 