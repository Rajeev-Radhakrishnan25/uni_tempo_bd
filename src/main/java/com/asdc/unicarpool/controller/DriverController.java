package com.asdc.unicarpool.controller;

import com.asdc.unicarpool.dto.request.CreateRideRequest;
import com.asdc.unicarpool.dto.request.UpdateRideRequestStatusRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.dto.response.RideRequestResponse;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.model.RideRequestStatus;
import com.asdc.unicarpool.model.RideStatus;
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
@RequestMapping("/api/v1/driver")
public class DriverController extends BaseController {

    private final IRideService rideService;
    private final IRideRequestService rideRequestService;

    @Autowired
    public DriverController(RideService rideService, IRideRequestService rideRequestService, JwtUtil jwtUtil) {
        super(jwtUtil);
        this.rideService = rideService;
        this.rideRequestService = rideRequestService;
    }

    @PostMapping("/create-ride")
    public ResponseEntity<RideResponse> bookARide(@Valid @RequestBody CreateRideRequest createRideRequest,
                                                  HttpServletRequest request) {
        String bannerId = extractBannerIdFromToken(request);
        RideResponse rideResponse = rideService.createNewRide(createRideRequest, bannerId);
        return ResponseEntity.ok(rideResponse);
    }

    @GetMapping("/ride")
    public ResponseEntity<List<RideResponse>> getActiveRides(HttpServletRequest request) {
        String bannerId = extractBannerIdFromToken(request);
        List<RideResponse> rideResponse = rideService.getActiveRidesByDriver(bannerId);
        return ResponseEntity.ok(rideResponse);
    }

    @GetMapping("/ride-request")
    public ResponseEntity<List<RideRequestResponse>> getPendingRideRequest(HttpServletRequest request) {
        String bannerId = extractBannerIdFromToken(request);
        List<RideRequestResponse> rideResponse = rideRequestService.getPendingRideRequestsByDriver(bannerId);
        return ResponseEntity.ok(rideResponse);
    }

    @PutMapping("/{rideRequestId}/accept")
    public ResponseEntity<BaseResponse> acceptRideRequest(@PathVariable Long rideRequestId, HttpServletRequest request) {
        String driverBannerId = extractBannerIdFromToken(request);
        UpdateRideRequestStatusRequest updateStatusRequest = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(rideRequestId)
                .driverBannerId(driverBannerId)
                .status(RideRequestStatus.ACCEPTED)
                .build();
        boolean response = rideRequestService.updateRideRequestStatus(updateStatusRequest);

        if (!response) {
            return ResponseEntity.badRequest().body(new BaseResponse("Unable to accept ride"));
        } else {
            return ResponseEntity.ok(new BaseResponse("Ride accepted successfully"));
        }
    }

    @PutMapping("/{rideRequestId}/reject")
    public ResponseEntity<BaseResponse> rejectRideRequest(@PathVariable Long rideRequestId, HttpServletRequest request) {
        String driverBannerId = extractBannerIdFromToken(request);
        UpdateRideRequestStatusRequest updateStatusRequest = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(rideRequestId)
                .driverBannerId(driverBannerId)
                .status(RideRequestStatus.REJECTED)
                .build();
        boolean response = rideRequestService.updateRideRequestStatus(updateStatusRequest);

        if (!response) {
            return ResponseEntity.badRequest().body(new BaseResponse("Unable to reject ride"));
        } else {
            return ResponseEntity.ok(new BaseResponse("Ride rejected successfully"));
        }
    }

    @GetMapping("/ride/{rideId}/status")
    public ResponseEntity<BaseResponse> updateRideStatus(@PathVariable Long rideId, @RequestParam String status, HttpServletRequest request) {
        String driverBannerId = extractBannerIdFromToken(request);
        boolean response = rideService.updateRideStatus(rideId, RideStatus.valueOf(status), driverBannerId);
        if (!response) {
            return ResponseEntity.badRequest().body(new BaseResponse("Unable to update ride status"));
        } else {
            return ResponseEntity.ok(new BaseResponse("Ride status updated successfully"));
        }
    }
}
