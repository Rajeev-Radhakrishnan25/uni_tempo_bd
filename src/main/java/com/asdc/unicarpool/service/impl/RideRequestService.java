package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.dto.request.RideRequestRequest;
import com.asdc.unicarpool.dto.request.UpdateRideRequestStatusRequest;
import com.asdc.unicarpool.dto.response.RideRequestResponse;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ValidationException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.RideRequest;
import com.asdc.unicarpool.model.RideRequestStatus;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.repository.IRideRepository;
import com.asdc.unicarpool.repository.IRideRequestRepository;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.IRideRequestService;
import com.asdc.unicarpool.util.EmailUtil.IEmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.asdc.unicarpool.constant.AppConstant.EmailConstant.Templates.RIDE_REQUEST_CHANGE_TEMPLATE;

@Service
@Slf4j
public class RideRequestService implements IRideRequestService {

    @Value("${spring.application.name}")
    private String appName;

    private final IRideRequestRepository rideRequestRepository;
    private final IRideRepository rideRepository;
    private final IUserRepository userRepository;
    private final IEmailUtil emailUtil;

    private final Mapper mapper;

    @Autowired
    public RideRequestService(IRideRequestRepository rideRequestRepository, IRideRepository rideRepository, IUserRepository userRepository, IEmailUtil emailUtil, Mapper mapper) {
        this.rideRequestRepository = rideRequestRepository;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.emailUtil = emailUtil;
        this.mapper = mapper;
    }

    @Override
    public RideRequestResponse createRideRequest(RideRequestRequest request, String riderBannerId) {
        User rider = userRepository.findByBannerId(riderBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Rider not found "));

        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new ValidationException("Ride not found "));

        if (!ride.getIsActive()) {
            throw new ValidationException("Cannot request a deleted ride");
        }

        if (ride.getDepartureDateTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot request a ride that has already departed");
        }

        if (ride.getDriver().getBannerId().equals(riderBannerId)) {
            throw new ValidationException("Driver cannot request their own ride");
        }

        if (rideRequestRepository.existsByRideAndRider(ride, rider)) {
            throw new ValidationException("You have already requested this ride");
        }

        if (ride.getAvailableSeats() <= 0) {
            throw new ValidationException("No available seats for this ride");
        }

        ride.setAvailableSeats(ride.getAvailableSeats() - 1);
        rideRepository.save(ride);

        RideRequest rideRequest = RideRequest.builder()
                .ride(ride)
                .rider(rider)
                .status(RideRequestStatus.PENDING)
                .message(request.getMessage())
                .build();

        RideRequest savedRequest = rideRequestRepository.save(rideRequest);

        return mapToRideRequestResponse(savedRequest);
    }

    @Override
    public List<RideRequestResponse> getPendingRideRequestsByDriver(String driverBannerId) {
        User driver = userRepository.findByBannerId(driverBannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Driver not found"));

        List<RideRequest> pendingRequests = rideRequestRepository.findByRideDriverAndStatus(
                driver,
                RideRequestStatus.PENDING
        );

        return pendingRequests.stream()
                .map(this::mapToRideRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateRideRequestStatus(UpdateRideRequestStatusRequest request) {
        User driver = userRepository.findByBannerId(request.getDriverBannerId())
                .orElseThrow(() -> new InvalidCredentialsException("Driver not found"));

        RideRequest rideRequest = rideRequestRepository.findByRideDriverAndId(driver, request.getRideRequestId())
                .orElseThrow(() -> new ValidationException("Ride not found"));

        if (rideRequest.getStatus() != RideRequestStatus.PENDING) {
            throw new ValidationException("Can only update requests in PENDING status");
        }

        if (request.getStatus() == RideRequestStatus.REJECTED) {
            Ride ride = rideRequest.getRide();
            ride.setAvailableSeats(ride.getAvailableSeats() + 1);
            rideRepository.save(ride);
        }
        rideRequest.setStatus(request.getStatus());
        RideRequest updateRideRequest = rideRequestRepository.save(rideRequest);
        sendStatusNotificationToRider(updateRideRequest);
        return true;
    }

    @Override
    public List<RideRequestResponse> getRideRequestsByRider(String bannerId) {
        User rider = userRepository.findByBannerId(bannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Rider not found"));

        List<RideRequestStatus> statuses = Arrays.asList(
                RideRequestStatus.PENDING,
                RideRequestStatus.ACCEPTED,
                RideRequestStatus.REJECTED
        );

        List<RideRequest> rideRequests = rideRequestRepository.findByRiderAndStatusIn(rider, statuses);

        return rideRequests.stream()
                .map(this::mapToRideRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RideRequestResponse> getCurrentBookingsForRider(String bannerId) {
        User rider = userRepository.findByBannerId(bannerId)
                .orElseThrow(() -> new InvalidCredentialsException("Rider not found"));

        List<RideRequest> rideRequests = rideRequestRepository.findCurrentBookingsForRider(rider);

        return rideRequests.stream()
                .map(this::mapToRideRequestResponse)
                .collect(Collectors.toList());
    }

    private void sendStatusNotificationToRider(RideRequest request) {
        String subject = request.getStatus() == RideRequestStatus.ACCEPTED ? "Ride Request Accepted - " + appName : "Ride Request Rejected - " + appName;

        Map<String, Object> templateData = Map.of(
                "riderName", request.getRider().getName(),
                "driverName", request.getRide().getDriver().getName(),
                "departureLocation", request.getRide().getDepartureLocation(),
                "destination", request.getRide().getDestination(),
                "departureTime", request.getRide().getDepartureDateTime(),
                "isAccepted", request.getStatus() == RideRequestStatus.ACCEPTED,
                "appName", appName
        );
        emailUtil.sendEmail(request.getRider().getEmail(), subject, RIDE_REQUEST_CHANGE_TEMPLATE, templateData);
    }

    private RideRequestResponse mapToRideRequestResponse(RideRequest request) {
        RideRequestResponse response = new RideRequestResponse();

        response.setRideId(request.getRide().getId());
        response.setRideRequestId(request.getId());

        response.setRiderBannerId(request.getRider().getBannerId());
        response.setRiderName(request.getRider().getName());
        response.setRiderPhoneNumber(request.getRider().getPhoneNumber());

        response.setDriverBannerId(request.getRide().getDriver().getBannerId());
        response.setDriverName(request.getRide().getDriver().getName());
        response.setDriverPhoneNumber(request.getRide().getDriver().getPhoneNumber());

        response.setDepartureLocation(request.getRide().getDepartureLocation());
        response.setDestination(request.getRide().getDestination());
        response.setMeetingPoint(request.getRide().getMeetingPoint());
        response.setDepartureDateTime(request.getRide().getDepartureDateTime());

        response.setStatus(request.getStatus().name());
        response.setMessage(request.getMessage());
        return response;
    }

}
