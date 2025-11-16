package com.asdc.unicarpool.controller;

import com.asdc.unicarpool.dto.request.RideRequestRequest;
import com.asdc.unicarpool.dto.response.RideRequestResponse;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.service.IRideRequestService;
import com.asdc.unicarpool.service.IRideService;
import com.asdc.unicarpool.service.impl.RideService;
import com.asdc.unicarpool.util.TokenUtil.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rider")
public class RiderController extends BaseController {

    private final IRideService rideService;
    private final IRideRequestService rideRequestService;

    @Autowired
    public RiderController(RideService rideService, IRideRequestService rideRequestService, JwtUtil jwtUtil) {
        super(jwtUtil);
        this.rideService = rideService;
        this.rideRequestService = rideRequestService;
    }

    @GetMapping("/ride/active")
    public ResponseEntity<List<RideResponse>> getActiveRides(HttpServletRequest request) {
        String bannerId = extractBannerIdFromToken(request);
        List<RideResponse> ride = rideService.getAllActiveRides(bannerId);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/book-ride")
    public ResponseEntity<RideRequestResponse> bookARide(@Valid @RequestBody RideRequestRequest rideRequest,
                                                         HttpServletRequest request) {
        String bannerId = extractBannerIdFromToken(request);
        RideRequestResponse response = rideRequestService.createRideRequest(rideRequest, bannerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-request")
    public ResponseEntity<List<RideRequestResponse>> bookARide(HttpServletRequest request) {
        String bannerId = extractBannerIdFromToken(request);
        List<RideRequestResponse> response = rideRequestService.getRideRequestsByRider(bannerId);
        return ResponseEntity.ok(response);
    }
}
