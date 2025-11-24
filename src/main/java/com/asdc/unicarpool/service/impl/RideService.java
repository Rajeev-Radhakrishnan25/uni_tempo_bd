package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.dto.request.CreateRideRequest;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ValidationException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.RideStatus;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import com.asdc.unicarpool.repository.IRideRepository;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.IRideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RideService implements IRideService {

    private final IUserRepository userRepository;
    private final IRideRepository rideRepository;
    private final Mapper mapper;

    @Autowired
    public RideService(IUserRepository userRepository, IRideRepository rideRepository, Mapper mapper) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
        this.mapper = mapper;
    }

    @Override
    public RideResponse createNewRide(CreateRideRequest request, String driverBannerId) {

        User driver = userRepository.findByBannerId(driverBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Driver not found "));

        if (!driver.getRoles().contains(UserRole.DRIVER)) {
            throw new InvalidCredentialsException("User is not a driver");
        }

        if (request.getDepartureDateTime().isBefore(LocalDateTime.now())) {
            throw new InvalidArgumentException("Departure time must be in the future");
        }

        Ride ride = Ride.builder()
                .driver(driver)
                .departureLocation(request.getDepartureLocation())
                .destination(request.getDestination())
                .departureDateTime(request.getDepartureDateTime())
                .availableSeats(request.getAvailableSeats())
                .meetingPoint(request.getMeetingPoint())
                .rideConditions(request.getRideConditions())
                .build();

        Ride savedRide = rideRepository.save(ride);

        RideResponse response = mapper.map(savedRide, RideResponse.class);

        response.setDriverName(savedRide.getDriver().getName());
        response.setDriverId(savedRide.getDriver().getBannerId());

        return response;
    }

    @Override
    public List<RideResponse> getActiveRidesByDriver(String driverBannerId) {
        User driver = userRepository.findByBannerId(driverBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Driver not found"));

        List<Ride> activeRides = rideRepository.findUpcomingRidesByDriver(driver);

        return activeRides.stream()
                .map(ride -> {
                    RideResponse response = mapper.map(ride, RideResponse.class);
                    response.setDriverName(ride.getDriver().getName());
                    response.setDriverId(ride.getDriver().getBannerId());
                    response.setStatus(ride.getStatus().getDisplayName());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RideResponse> getAllActiveRides(String bannerId){

        List<Ride> activeRides = rideRepository.listAllActiveRides(LocalDateTime.now());

        return activeRides.stream()
                .filter(ride -> !ride.getDriver().getBannerId().equals(bannerId))
                .map(ride -> {
                    RideResponse response = mapper.map(ride, RideResponse.class);
                    response.setDriverName(ride.getDriver().getName());
                    response.setDriverId(ride.getDriver().getBannerId());
                    response.setStatus(ride.getStatus().getDisplayName());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateRideStatus(Long rideId, RideStatus newStatus, String driverBannerId) {
        User driver = userRepository.findByBannerId(driverBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Driver not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()->new ValidationException("Ride not found"));

        if (!ride.getDriver().getBannerId().equals(driver.getBannerId())) {
            throw new ValidationException("Driver is not authorized to update this ride");
        }

        if( ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new ValidationException("Cannot update a completed or cancelled ride");
        }
        if (ride.getStatus() == RideStatus.WAITING && newStatus == RideStatus.COMPLETED) {
            throw new ValidationException("Cannot mark a waiting ride as completed");
        }

        ride.setStatus(newStatus);
        rideRepository.save(ride);

        return true;
    }

}
