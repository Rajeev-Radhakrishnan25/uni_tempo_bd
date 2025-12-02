package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.CreateRideRequest;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.model.RideStatus;
import com.asdc.unicarpool.model.User;

import java.util.List;

public interface IRideService {
    RideResponse createNewRide(CreateRideRequest request, String driverBannerId);

    List<RideResponse> getActiveRidesByDriver(String driverBannerId);

    List<RideResponse> getAllActiveRides(String driverBannerId);

    boolean updateRideStatus(Long rideId, RideStatus newStatus, String driverBannerId);
    User getUserByBannerId(String bannerId);
}
