package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.CreateRideRequest;
import com.asdc.unicarpool.dto.response.RideResponse;

import java.util.List;

public interface IRideService {
    RideResponse createNewRide(CreateRideRequest request, String driverBannerId);
    List<RideResponse> getActiveRidesByDriver(String driverBannerId);
    List<RideResponse> getAllActiveRides(String driverBannerId);
}
