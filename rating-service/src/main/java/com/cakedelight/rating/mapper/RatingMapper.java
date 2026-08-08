package com.cakedelight.rating.mapper;

import com.cakedelight.rating.dto.RatingRequest;
import com.cakedelight.rating.dto.RatingResponse;
import com.cakedelight.rating.entity.Rating;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RatingMapper {

    public Rating toEntity(RatingRequest request) {
        Rating rating = new Rating();
        rating.setCakeId(request.getCakeId());
        rating.setUserId(request.getUserId());
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        return rating;
    }

    public RatingResponse toResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getCakeId(),
                rating.getUserId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }
}
