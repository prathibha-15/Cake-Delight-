package com.cakedelight.rating.service.impl;

import com.cakedelight.rating.client.UserServiceClient;
import com.cakedelight.rating.dto.AverageRatingResponse;
import com.cakedelight.rating.dto.RatingRequest;
import com.cakedelight.rating.dto.RatingResponse;
import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.exception.ResourceNotFoundException;
import com.cakedelight.rating.mapper.RatingMapper;
import com.cakedelight.rating.repository.RatingRepository;
import com.cakedelight.rating.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final UserServiceClient userServiceClient;

    public RatingServiceImpl(RatingRepository ratingRepository, RatingMapper ratingMapper, UserServiceClient userServiceClient) {
        this.ratingRepository = ratingRepository;
        this.ratingMapper = ratingMapper;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public RatingResponse createRating(RatingRequest request) {
        Rating rating = ratingMapper.toEntity(request);
        Rating saved = ratingRepository.save(rating);
        RatingResponse response = ratingMapper.toResponse(saved);
        response.setUsername(userServiceClient.getUsername(saved.getUserId()));
        return response;
    }

    @Override
    public List<RatingResponse> getRatingsByCakeId(Long cakeId) {
        // Dedupe username lookups per unique userId within this request.
        Map<Long, String> usernameCache = new HashMap<>();
        return ratingRepository.findByCakeId(cakeId)
                .stream()
                .map(rating -> {
                    RatingResponse response = ratingMapper.toResponse(rating);
                    response.setUsername(usernameCache.computeIfAbsent(rating.getUserId(), userServiceClient::getUsername));
                    return response;
                })
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
