package com.asdc.unicarpool.service;

import com.asdc.unicarpool.model.RatingReview;
import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.RideStatus;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.repository.IRatingReviewRepository;
import com.asdc.unicarpool.repository.IRideRepository;
import com.asdc.unicarpool.service.impl.RatingReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RatingReviewServiceTest {

    private IRideRepository rideRepository;
    private RatingReviewService ratingReviewService;
    private IRatingReviewRepository ratingReviewRepository;

    private Ride ride;
    private User driver;

    @BeforeEach
    void setUp() {
        rideRepository = mock(IRideRepository.class);
        ratingReviewRepository = mock(IRatingReviewRepository.class);
        ratingReviewService = new RatingReviewService(ratingReviewRepository, rideRepository);

        driver = new User();
        driver.setId(100L);

        ride = new Ride();
        ride.setId(1L);
        ride.setDriver(driver);
        ride.setStatus(RideStatus.COMPLETED);
    }

    @Test
    void submitReview_success() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(ratingReviewRepository.findByRideIdAndPassengerId(1L, 100L)).thenReturn(null);

        RatingReview result = ratingReviewService.submitReview(1L, 100L, 5, "10/10 Ride to Dalhousie");

        assertEquals(5, result.getRating());
        assertEquals("10/10 Ride to Dalhousie", result.getComment());
        assertEquals(driver.getId(), result.getDriverId());
        assertEquals(100L, result.getPassengerId());
        assertEquals(ride, result.getRide());
        assertEquals(true, ride.getReviewed());
    }

    @Test
    void submitReview_rideNotFound() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ratingReviewService.submitReview(1L, 100L, 4, "Test"));


        assertEquals("Ride not found", exception.getMessage());
    }

    @Test
    void submitReview_rideNotCompleted() {
        ride.setStatus(RideStatus.STARTED);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ratingReviewService.submitReview(1L, 100L, 4, "Test"));

        assertEquals("Ride not completed", exception.getMessage());
    }


    @Test
    void submitReview_alreadySubmitted() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(ratingReviewRepository.findByRideIdAndPassengerId(1L, 100L)).thenReturn(new RatingReview());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                ratingReviewService.submitReview(1L, 100L, 4, "Test"));

        assertEquals("Ride has already been submitted", exception.getMessage());
    }

    @Test
    void submitReview_invalidRating() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ratingReviewService.submitReview(1L, 100L, 0, "Test"));

        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void getAverageRating() {
        RatingReview rating1 = new RatingReview();
        rating1.setRating(5);

        RatingReview rating2 = new RatingReview();
        rating2.setRating(4);

        when(ratingReviewRepository.findByDriverId(driver.getId()))
                .thenReturn(List.of(rating1, rating2));

        double average = ratingReviewService.getAverageRating(driver.getId());
        assertEquals(4.5, average);

    }

    @Test
    void getAverageRating_noReviews() {
        when(ratingReviewRepository.findByDriverId(driver.getId()))
                .thenReturn(List.of());

        double average = ratingReviewService.getAverageRating(driver.getId());
        assertEquals(0.0, average);
    }
}