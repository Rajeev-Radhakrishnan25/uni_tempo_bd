package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.dto.request.RideRequestRequest;
import com.asdc.unicarpool.dto.request.UpdateRideRequestStatusRequest;
import com.asdc.unicarpool.dto.response.RideRequestResponse;
import com.asdc.unicarpool.dto.response.RideRequestStatusResponse;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ValidationException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.*;
import com.asdc.unicarpool.repository.*;
import com.asdc.unicarpool.service.IRideRequestService;
import com.asdc.unicarpool.util.EmailUtil.IEmailUtil;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.asdc.unicarpool.constant.AppConstant.EMAIL_TEMPLATE_RIDE_REQUEST_CHANGE;

@Service
@Slf4j
public class RideRequestService extends BaseService implements IRideRequestService {

    @Value("${spring.application.name}")
    private String appName;

    private final IRideRequestRepository rideRequestRepository;
    private final IRideRepository rideRepository;
    private final IUserRepository userRepository;
    private final IEmailUtil emailUtil;
    private final IRatingReviewRepository  ratingReviewRepository;
    private final ICabBookingRepository  cabBookingRepository;

    private final Mapper mapper;

    @Autowired
    public RideRequestService(IRideRequestRepository rideRequestRepository, IRideRepository rideRepository, IUserRepository userRepository, IEmailUtil emailUtil, Mapper mapper, IRatingReviewRepository ratingReviewRepository, ICabBookingRepository cabBookingRepository) {
        this.rideRequestRepository = rideRequestRepository;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.emailUtil = emailUtil;
        this.mapper = mapper;
        this.ratingReviewRepository = ratingReviewRepository;
        this.cabBookingRepository = cabBookingRepository;
    }

    @Override
    public RideRequestResponse createRideRequest(RideRequestRequest request, String riderBannerId) {
        User rider = findRiderByBannerIdOrThrow(userRepository, riderBannerId);

        Ride ride = findRideByIdOrThrow(rideRepository, request.getRideId());

        if (Boolean.FALSE.equals(ride.getIsActive())) {
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
        User driver = findDriverByBannerIdOrThrow(userRepository, driverBannerId);

        List<RideRequest> pendingRequests = rideRequestRepository.findByRideDriverAndStatus(
                driver,
                RideRequestStatus.PENDING
        );

        List<RideRequestResponse> result = pendingRequests.stream()
                .map(this::mapToRideRequestResponse)
                .collect(Collectors.toList());

        List<CabBooking> cabBookings = cabBookingRepository.findByRiderAndStatusIn(
                driver,
                List.of(CabBookingStatus.PENDING, CabBookingStatus.CONFIRMED)
        );

        for(CabBooking cabBooking : cabBookings) {
            RideRequestResponse response = new RideRequestResponse();

            response.setRideRequestId(null);
            response.setRideId(cabBooking.getId());

            response.setRiderBannerId(cabBooking.getRider().getBannerId());
            response.setRiderName(cabBooking.getRider().getName());
            response.setRiderPhoneNumber(cabBooking.getRider().getPhoneNumber());

            response.setDriverBannerId(cabBooking.getDriver().getBannerId());
            response.setDriverName(cabBooking.getDriver().getName());
            response.setDriverPhoneNumber(cabBooking.getDriver().getPhoneNumber());

            response.setDepartureLocation(cabBooking.getPickupLocation());
            response.setDestination(cabBooking.getDropoffLocation());
            response.setDepartureDateTime(cabBooking.getArrivalTime());

            response.setStatus(cabBooking.getStatus().name());

            response.setMessage("CAB");

            response.setReviewed(false);

            result.add(response);
        }
        return result;
    }

    @Override
    public boolean updateRideRequestStatus(UpdateRideRequestStatusRequest request) {
        User driver = findDriverByBannerIdOrThrow(userRepository, request.getDriverBannerId());

        RideRequest rideRequest = rideRequestRepository.findByRideDriverAndId(driver, request.getRideRequestId())
                .orElseThrow(() -> new ValidationException("Ride request not found"));

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
        User rider = findRiderByBannerIdOrThrow(userRepository, bannerId);

        List<RideRequestStatus> statuses = Arrays.asList(
                RideRequestStatus.PENDING,
                RideRequestStatus.ACCEPTED,
                RideRequestStatus.REJECTED
        );

        List<RideRequest> rideRequests = rideRequestRepository.findByRiderAndStatusIn(rider, statuses);

        return rideRequests.stream()
                .map(this::mapToRideRequestResponse)
                .toList();
    }


    @Override
    public List<RideRequestStatusResponse> getConfirmedRides(String bannerId) {

        User rider = findRiderByBannerIdOrThrow(userRepository, bannerId);

        List<RideRequest> rideRequests = rideRequestRepository.findCompletedBookingsForRider(rider);

        List<RideRequestStatusResponse> result = rideRequests.stream()
                .map(this::mapToRideRequestStatusResponse)
                .collect(Collectors.toList());

        List<CabBooking> cabBookings = cabBookingRepository.findByRiderAndStatusIn(rider, List.of(CabBookingStatus.COMPLETED));

        for(CabBooking cabBooking : cabBookings) {
            RideRequestStatusResponse cab = new  RideRequestStatusResponse();

            cab.setRideId(cabBooking.getId());
            cab.setDriverName(cabBooking.getDriver().getName());
            cab.setDriverPhoneNumber(cabBooking.getDriver().getPhoneNumber());

            cab.setDepartureLocation(cabBooking.getPickupLocation());
            cab.setDestination(cabBooking.getDropoffLocation());
            cab.setDepartureDateTime(cabBooking.getArrivalTime());

            cab.setRideRequestStatus("COMPLETED");
            cab.setRideRequestStatus("COMPLETED");
            cab.setReviewed(false);

            result.add(cab);
        }
        return result;
    }

    @Override
    public List<RideRequestStatusResponse> getCurrentBookingsForRider(String bannerId) {
        User rider = findRiderByBannerIdOrThrow(userRepository, bannerId);

        List<RideRequest> rideRequests = rideRequestRepository.findCurrentBookingsForRider(rider);
        List<RideRequestStatusResponse> result = rideRequests.stream()
                .map(this::mapToRideRequestStatusResponse)
                .collect(Collectors.toList());

        List<CabBooking> cabBookings = cabBookingRepository.findByRiderAndStatusIn(
                rider,
                List.of(CabBookingStatus.PENDING, CabBookingStatus.CONFIRMED)
        );

        for (CabBooking cabBooking : cabBookings) {
            RideRequestStatusResponse cab = new RideRequestStatusResponse();

            cab.setRideId(cabBooking.getId());
            cab.setDriverName(cabBooking.getDriver().getName());
            cab.setDriverPhoneNumber(cabBooking.getDriver().getPhoneNumber());

            cab.setDepartureLocation(cabBooking.getPickupLocation());
            cab.setDestination(cabBooking.getDropoffLocation());
            cab.setDepartureDateTime(cabBooking.getArrivalTime());

            cab.setRideRequestStatus(cabBooking.getStatus().name());
            cab.setRideStatus(cabBooking.getStatus().name());

            cab.setReviewed(false);

            result.add(cab);
        }
        return result;
    }

    private void sendStatusNotificationToRider(RideRequest request) {
        String subject = buildRideRequestSubject(request.getStatus());
        Map<String, Object> templateData = buildRideRequestEmailData(request);
        emailUtil.sendEmail(request.getRider().getEmail(), subject, EMAIL_TEMPLATE_RIDE_REQUEST_CHANGE, templateData);
    }
    private String buildRideRequestSubject(RideRequestStatus status) {
        return status == RideRequestStatus.ACCEPTED
                ? "Ride Request Accepted - " + appName
                : "Ride Request Rejected - " + appName;
    }
    private Map<String, Object> buildRideRequestEmailData(RideRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("riderName", request.getRider().getName());
        data.put("driverName", request.getRide().getDriver().getName());
        data.put("departureLocation", request.getRide().getDepartureLocation());
        data.put("destination", request.getRide().getDestination());
        data.put("departureTime", request.getRide().getDepartureDateTime());
        data.put("isAccepted", request.getStatus() == RideRequestStatus.ACCEPTED);
        data.put("appName", appName);
        return data;
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

        boolean reviewed = ratingReviewRepository.findByRideIdAndPassengerId(
                request.getRide().getId(),
                request.getRider().getId()
        ) != null;
        response.setReviewed(reviewed);

        return response;
    }

    private RideRequestStatusResponse mapToRideRequestStatusResponse(RideRequest request) {
        RideRequestStatusResponse response = new RideRequestStatusResponse();

        response.setRideId(request.getRide().getId());
        response.setRideRequestId(request.getId());

        response.setDriverBannerId(request.getRide().getDriver().getBannerId());
        response.setDriverName(request.getRide().getDriver().getName());
        response.setDriverPhoneNumber(request.getRide().getDriver().getPhoneNumber());

        response.setDepartureLocation(request.getRide().getDepartureLocation());
        response.setDestination(request.getRide().getDestination());
        response.setMeetingPoint(request.getRide().getMeetingPoint());
        response.setDepartureDateTime(request.getRide().getDepartureDateTime());

        response.setRideRequestStatus(request.getStatus().name());
        response.setRideStatus(request.getRide().getStatus().name());

        boolean reviewed = ratingReviewRepository.findByRideIdAndPassengerId(
                request.getRide().getId(),
                request.getRider().getId()
        ) != null;

        response.setReviewed(reviewed);

        return response;
    }
}
