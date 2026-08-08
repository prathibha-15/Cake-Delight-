package com.cakedelight.rating.service.impl;

import com.cakedelight.rating.dto.AverageRatingResponse;
import com.cakedelight.rating.dto.RatingRequest;
import com.cakedelight.rating.dto.RatingResponse;
import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.exception.ResourceNotFoundException;
import com.cakedelight.rating.mapper.RatingMapper;
import com.cakedelight.rating.repository.RatingRepository;
import com.cakedelight.rating.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    public RatingServiceImpl(RatingRepository ratingRepository, RatingMapper ratingMapper) {
        this.ratingRepository = ratingRepository;
        this.ratingMapper = ratingMapper;
    }

    @Override
    public RatingResponse createRating(RatingRequest request) {
        Rating rating = ratingMapper.toEntity(request);
        Rating saved = ratingRepository.save(rating);
        return ratingMapper.toResponse(saved);
    }

    @Override
    public List<RatingResponse> getRatingsByCakeId(Long cakeId) {
        return ratingRepository.findByCakeId(cakeId)
                .stream()
                .map(ratingMapper::toResponse)
                .toList();
    }

    @Override
    public AverageRatingResponse getAverageRatingByCakeId(Long cakeId) {
        List<Rating> ratings = ratingRepository.findByCakeId(cakeId);
        if (ratings.isEmpty()) {
            throw new ResourceNotFoundException("No ratings found for cake id: " + cakeId);
        }

        Double average = ratingRepository.findAverageScoreByCakeId(cakeId);
        long count = ratings.size();

        return new AverageRatingResponse(cakeId, average, count);
    }
}
