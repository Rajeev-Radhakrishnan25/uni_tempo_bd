package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.dto.request.CreateRideRequest;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ValidationException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.*;
import com.asdc.unicarpool.repository.IRatingReviewRepository;
import com.asdc.unicarpool.repository.IRideRepository;
import com.asdc.unicarpool.repository.IRideRequestRepository;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.IRideService;
import com.asdc.unicarpool.util.EmailUtil.IEmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.asdc.unicarpool.constant.AppConstant.EMAIL_TEMPLATE_RIDE_STATUS;

@Service
@Slf4j
public class RideService extends BaseService implements IRideService {
    @Value("${spring.application.name}")
    private String appName;

    private final IUserRepository userRepository;
    private final IRideRepository rideRepository;
    private final IRideRequestRepository rideRequestRepository;
    private final IEmailUtil emailUtil;
    private final IRatingReviewRepository ratingReviewRepository;
    private final Mapper mapper;

    @Autowired
    public RideService(IUserRepository userRepository, IRideRepository rideRepository, IRideRequestRepository rideRequestRepository, IEmailUtil emailUtil, IRatingReviewRepository ratingReviewRepository, Mapper mapper) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
        this.rideRequestRepository = rideRequestRepository;
        this.emailUtil = emailUtil;
        this.ratingReviewRepository = ratingReviewRepository;
        this.mapper = mapper;
    }

    @Override
    public RideResponse createNewRide(CreateRideRequest request, String driverBannerId) {

        User driver = findDriverByBannerIdOrThrow(userRepository, driverBannerId);

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
        User driver = findDriverByBannerIdOrThrow(userRepository, driverBannerId);

        List<Ride> activeRides = rideRepository.findUpcomingRidesByDriver(driver);

        return activeRides.stream()
                .map(ride -> {
                    RideResponse response = mapper.map(ride, RideResponse.class);
                    response.setDriverName(ride.getDriver().getName());
                    response.setDriverId(ride.getDriver().getBannerId());
                    response.setStatus(ride.getStatus().name());
                    return response;
                })
                .toList();
    }

    @Override
    public List<RideResponse> getAllActiveRides(String bannerId) {

        List<Ride> activeRides = rideRepository.listAllActiveRides(LocalDateTime.now());

        return activeRides.stream()
                .filter(ride -> !ride.getDriver().getBannerId().equals(bannerId))
                .map(ride -> {
                    RideResponse response = mapper.map(ride, RideResponse.class);
                    response.setDriverName(ride.getDriver().getName());
                    response.setDriverId(ride.getDriver().getBannerId());
                    response.setStatus(ride.getStatus().name());

                    boolean reviewed = ratingReviewRepository.findByRideIdAndPassengerId(
                            ride.getId(),
                            getUserByBannerId(bannerId).getId()) != null;

                    response.setReviewed(reviewed);
                    return response;
                })
                .toList();
    }

    @Override
    public boolean updateRideStatus(Long rideId, RideStatus newStatus, String driverBannerId) {
        User driver = findDriverByBannerIdOrThrow(userRepository, driverBannerId);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ValidationException("Ride not found"));

        if (!ride.getDriver().getBannerId().equals(driver.getBannerId())) {
            throw new ValidationException("Driver is not authorized to update this ride");
        }

        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new ValidationException("Cannot update a completed or cancelled ride");
        }
        if (ride.getStatus() == RideStatus.WAITING && newStatus == RideStatus.COMPLETED) {
            throw new ValidationException("Cannot mark a waiting ride as completed");
        }

        ride.setStatus(newStatus);
        notifyRidersOfStatusChange(ride, newStatus);
        rideRepository.save(ride);

        return true;
    }

    @Override
    public User getUserByBannerId(String bannerId) {
        return userRepository.findByBannerId(bannerId)
                .orElse(null);
    }

    private void notifyRidersOfStatusChange(Ride ride, RideStatus newStatus) {
        List<RideRequest> acceptedRequests = rideRequestRepository.findByRideAndStatus(
                ride, RideRequestStatus.ACCEPTED
        );
        for (RideRequest request : acceptedRequests) {
            Map<String, Object> templateData = getStringObjectMap(ride, newStatus, request);
            String subject = "Ride Status Update - " + appName;
            emailUtil.sendEmail(request.getRider().getEmail(), subject, EMAIL_TEMPLATE_RIDE_STATUS, templateData);
        }
    }

    private Map<String, Object> getStringObjectMap(Ride ride, RideStatus newStatus, RideRequest request) {
        User rider = request.getRider();
        Map<String, Object> data = new HashMap<>();
        data.put("riderName", rider.getName());
        data.put("driverName", ride.getDriver().getName());
        data.put("departureLocation", ride.getDepartureLocation());
        data.put("destination", ride.getDestination());
        data.put("departureTime", ride.getDepartureDateTime());
        data.put("newStatus", newStatus);
        data.put("appName", appName);
        return data;
    }

}


