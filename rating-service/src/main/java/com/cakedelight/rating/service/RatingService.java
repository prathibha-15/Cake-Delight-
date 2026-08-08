package com.cakedelight.rating.service;

import com.cakedelight.rating.dto.AverageRatingResponse;
import com.cakedelight.rating.dto.RatingRequest;
import com.cakedelight.rating.dto.RatingResponse;

import java.util.List;

public interface RatingService {

    RatingResponse createRating(RatingRequest request);

    List<RatingResponse> getRatingsByCakeId(Long cakeId);

    AverageRatingResponse getAverageRatingByCakeId(Long cakeId);
}
