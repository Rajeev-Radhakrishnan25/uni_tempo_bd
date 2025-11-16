package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.RideRequestRequest;
import com.asdc.unicarpool.dto.request.UpdateRideRequestStatusRequest;
import com.asdc.unicarpool.dto.response.RideRequestResponse;

import java.util.List;

public interface IRideRequestService {

    RideRequestResponse createRideRequest(RideRequestRequest request, String riderBannerId);

    List<RideRequestResponse> getPendingRideRequestsByDriver(String driverBannerId);

    boolean updateRideRequestStatus(UpdateRideRequestStatusRequest request);

    List<RideRequestResponse> getRideRequestsByRider(String bannerId);
}
