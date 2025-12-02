package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.CabBookingRequest;
import com.asdc.unicarpool.dto.response.CabBookingResponse;
import com.asdc.unicarpool.model.User;

import java.util.Optional;

public interface ICabBookingService {

    CabBookingResponse createCabBooking(CabBookingRequest request, String riderBannerId);
    Optional<User> findAvailableDriver();
}
