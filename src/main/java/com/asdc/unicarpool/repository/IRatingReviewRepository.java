package com.asdc.unicarpool.repository;

import com.asdc.unicarpool.model.RatingReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRatingReviewRepository  extends JpaRepository<RatingReview, Long> {
    //This gets all reviews for the driver for our dashboard stats
    List<RatingReview> findByDriverId(Long ride);

    //This checks if a passenger already rated this ride or not
    RatingReview findByRideIdAndPassengerId(long rideId, long passengerId);
}