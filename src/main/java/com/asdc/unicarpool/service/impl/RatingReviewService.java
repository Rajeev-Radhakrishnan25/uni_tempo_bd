package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.model.RatingReview;
import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.RideStatus;
import com.asdc.unicarpool.repository.IRatingReviewRepository;
import com.asdc.unicarpool.repository.IRideRepository;
import com.asdc.unicarpool.service.IRatingReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingReviewService implements IRatingReviewService {

    private final IRatingReviewRepository ratingReviewRepository;
    private final IRideRepository rideRepository;

    public RatingReviewService(IRatingReviewRepository ratingReviewRepository, IRideRepository rideRepository) {
        this.ratingReviewRepository = ratingReviewRepository;
        this.rideRepository = rideRepository;
    }

    @Override
    public RatingReview submitReview(Long rideId, Long passengerId, int rating, String comment) {
        if(rating < AppConstant.MIN_RATING || rating > AppConstant.MAX_RATING) {
            throw new IllegalArgumentException(
                String.format("Rating must be between %d and %d", AppConstant.MIN_RATING, AppConstant.MAX_RATING)
            );
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        if(ride.getStatus() != RideStatus.COMPLETED) {
            throw new IllegalArgumentException("Ride not completed");
        }

        RatingReview ratingReview = ratingReviewRepository.findByRideIdAndPassengerId(rideId,passengerId);

        if(ratingReview != null) {
            throw new IllegalStateException("Ride has already been submitted");
        }

        RatingReview review =  new RatingReview();
        review.setRide(ride);
        review.setPassengerId(passengerId);
        review.setDriverId(ride.getDriver().getId());
        review.setRating(rating);
        review.setComment(comment);

        ratingReviewRepository.save(review);
        ride.setReviewed(true);
        rideRepository.save(ride);

        return review;
    }

    @Override
    public double getAverageRating(long driverId) {
        List<RatingReview> ratingReviews = ratingReviewRepository.findByDriverId(driverId);
        if(ratingReviews.isEmpty()) return 0.0;

        double average =  ratingReviews.stream()
                            .mapToInt(RatingReview ::getRating)
                            .average()
                            .orElse(0.0);


        String rounded = String.format("%.1f", average);

        return Double.parseDouble(rounded);
    }
}
