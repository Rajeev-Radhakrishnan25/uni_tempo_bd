package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.dto.request.CabBookingRequest;
import com.asdc.unicarpool.dto.response.CabBookingResponse;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.CabBooking;
import com.asdc.unicarpool.model.CabBookingStatus;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import com.asdc.unicarpool.repository.ICabBookingRepository;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.ICabBookingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CabBookingService implements ICabBookingService {

    private final ICabBookingRepository cabBookingRepository;
    private final IUserRepository userRepository;
    private final Mapper mapper;

    private static final int ETA_MINUTES_BASIC = 5;
    private static final double ESTIMATED_FARE_TOTAL = 10.0;

    public CabBookingService(ICabBookingRepository cabBookingRepository,
                             IUserRepository userRepository,
                             Mapper mapper) {
        this.cabBookingRepository = cabBookingRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public CabBookingResponse createCabBooking(CabBookingRequest request, String riderBannerId) {

        User rider =  userRepository.findByBannerId(riderBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        Optional<User> optionalDriver = findAvailableDriver();

        if (optionalDriver.isEmpty()) {
            return CabBookingResponse.builder()
                    .status("NO_DRIVERS_AVAILABLE")
                    .build();
        }

        User driver = optionalDriver.get();

        int etaMinutes = ETA_MINUTES_BASIC;
        double estimatedFare = ESTIMATED_FARE_TOTAL;

        LocalDateTime arrivalTime = LocalDateTime.now().plusMinutes(etaMinutes);

        CabBooking booking = CabBooking.builder()
                .rider(rider)
                .driver(driver)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .passengerCount(request.getPassengerCount())
                .status(CabBookingStatus.CONFIRMED)
                .estimatedFare(estimatedFare)
                .etaMinutes(etaMinutes)
                .arrivalTime(arrivalTime)
                .build();

        booking = cabBookingRepository.save(booking);

        CabBookingResponse response = mapper.map(booking, CabBookingResponse.class);

        response.setDriverId(booking.getDriver().getId());
        response.setDriverName(booking.getDriver().getName());

        return response;
    }

    @Override
    public Optional<User> findAvailableDriver(){
        List<User> availableDrivers = userRepository.findByRolesContainingAndIsAvailableTrue(UserRole.DRIVER);

        if(availableDrivers.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(availableDrivers.get(0));
    }

}
