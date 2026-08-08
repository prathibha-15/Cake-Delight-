package com.cakedelight.rating.repository;

import com.cakedelight.rating.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByCakeId(Long cakeId);

    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Rating r WHERE r.cakeId = :cakeId")
    Double findAverageScoreByCakeId(@Param("cakeId") Long cakeId);
}
