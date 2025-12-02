package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ValidationException;
import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.repository.IRideRepository;
import com.asdc.unicarpool.repository.IUserRepository;

public abstract class BaseService {
    protected User findUserByBannerIdOrThrow(IUserRepository userRepository, String bannerId) {
        return userRepository.findByBannerId(bannerId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    protected User findDriverByBannerIdOrThrow(IUserRepository userRepository, String driverBannerId) {
        return userRepository.findByBannerId(driverBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Driver not found"));
    }

    protected User findRiderByBannerIdOrThrow(IUserRepository userRepository, String riderBannerId) {
        return userRepository.findByBannerId(riderBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Rider not found"));
    }

    protected Ride findRideByIdOrThrow(IRideRepository rideRepository, Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ValidationException("Ride not found"));
    }

}
