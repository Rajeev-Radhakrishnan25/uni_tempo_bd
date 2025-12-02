package com.asdc.unicarpool.controller;

import com.asdc.unicarpool.dto.request.CabBookingRequest;
import com.asdc.unicarpool.dto.request.RideRequestRequest;
import com.asdc.unicarpool.dto.request.SubmitReviewRequest;
import com.asdc.unicarpool.dto.response.CabBookingResponse;
import com.asdc.unicarpool.dto.response.RideRequestResponse;
import com.asdc.unicarpool.dto.response.RideRequestStatusResponse;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.component.TokenExtractor;
import com.asdc.unicarpool.service.ICabBookingService;
import com.asdc.unicarpool.service.IRatingReviewService;
import com.asdc.unicarpool.service.IRideRequestService;
import com.asdc.unicarpool.service.IRideService;
import com.asdc.unicarpool.service.impl.RideService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rider")
public class RiderController {

    private final IRideService rideService;
    private final IRideRequestService rideRequestService;
    private final IRatingReviewService ratingReviewService;
    private final ICabBookingService  cabBookingService;
    private final TokenExtractor tokenExtractor;

    @Autowired
    public RiderController(RideService rideService, IRideRequestService rideRequestService, IRatingReviewService ratingReviewService,ICabBookingService cabBookingService, TokenExtractor tokenExtractor) {
        this.rideService = rideService;
        this.rideRequestService = rideRequestService;
        this.ratingReviewService = ratingReviewService;
        this.cabBookingService = cabBookingService;
        this.tokenExtractor = tokenExtractor;
    }

    @GetMapping("/ride/active")
    public ResponseEntity<List<RideResponse>> getActiveRides(HttpServletRequest request) {
        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        List<RideResponse> ride = rideService.getAllActiveRides(bannerId);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/book-ride")
    public ResponseEntity<RideRequestResponse> bookARide(@Valid @RequestBody RideRequestRequest rideRequest,
                                                         HttpServletRequest request) {
        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        RideRequestResponse response = rideRequestService.createRideRequest(rideRequest, bannerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-request")
    public ResponseEntity<List<RideRequestResponse>> bookARide(HttpServletRequest request) {
        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        List<RideRequestResponse> response = rideRequestService.getRideRequestsByRider(bannerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/completed-bookings")
    public ResponseEntity<List<RideRequestStatusResponse>> getCompletedBookings(HttpServletRequest request) {
        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        List<RideRequestStatusResponse> confirmedRides = rideRequestService.getConfirmedRides(bannerId);
        return ResponseEntity.ok(confirmedRides);
    }

    @GetMapping("/current-bookings")
    public ResponseEntity<List<RideRequestStatusResponse>> getCurrentBookings(HttpServletRequest request) {
        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        List<RideRequestStatusResponse> response = rideRequestService.getCurrentBookingsForRider(bannerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rating")
    public ResponseEntity<String> submitReview(@RequestBody SubmitReviewRequest request, HttpServletRequest httpServletRequest){
        String bannerId = tokenExtractor.extractBannerIdFromToken(httpServletRequest);
        User passenger = rideService.getUserByBannerId(bannerId);

        if(passenger == null){
            return ResponseEntity.badRequest().body("User not found");
        }

        try{
            ratingReviewService.submitReview(
                    request.getRideId(),
                    passenger.getId(),
                    request.getRating(),
                    request.getComment()
            );
        } catch (IllegalArgumentException e){
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Review Submitted Successfully");
    }

    @PostMapping("/cab/book")
    public ResponseEntity<CabBookingResponse> bookCab(@RequestBody CabBookingRequest request, HttpServletRequest httpServletRequest){
        String bannerId = tokenExtractor.extractBannerIdFromToken(httpServletRequest);
        CabBookingResponse response = cabBookingService.createCabBooking(request, bannerId);
        return ResponseEntity.ok(response);
    }

}