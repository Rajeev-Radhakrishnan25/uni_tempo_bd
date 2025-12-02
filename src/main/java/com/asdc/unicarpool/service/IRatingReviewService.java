package com.asdc.unicarpool.service;

import com.asdc.unicarpool.model.RatingReview;

public interface IRatingReviewService {

    RatingReview submitReview(Long rideId, Long passengerId, int rating, String comment);
    double getAverageRating(long rideId);
}
