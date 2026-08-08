package com.cakedelight.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    @NotNull(message = "cakeId is required")
    private Long cakeId;

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "score is required")
    @Min(value = 1, message = "score must be between 1 and 5")
    @Max(value = 5, message = "score must be between 1 and 5")
    private Integer score;

    private String comment;
}
