package com.cakedelight.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private Long id;
    private Long cakeId;
    private Long userId;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}
