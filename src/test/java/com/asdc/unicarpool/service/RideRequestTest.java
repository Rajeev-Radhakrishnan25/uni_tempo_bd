package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.RideRequestRequest;
import com.asdc.unicarpool.dto.request.UpdateRideRequestStatusRequest;
import com.asdc.unicarpool.dto.response.RideRequestResponse;
import com.asdc.unicarpool.dto.response.RideRequestStatusResponse;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ValidationException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.*;
import com.asdc.unicarpool.repository.*;
import com.asdc.unicarpool.service.impl.RideRequestService;
import com.asdc.unicarpool.util.EmailUtil.IEmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RideRequestTest {

    private IRideRequestRepository rideRequestRepository;
    private IRideRepository rideRepository;
    private IUserRepository userRepository;
    private IEmailUtil emailUtil;

    private RideRequestService rideRequestService;

    private User testDriver;
    private User testRider;
    private Ride testRide;
    private RideRequestRequest rideRequestDto;
    private RideRequest rideRequest;
    private RideRequestResponse rideRequestResponse;
    private String bannerId;
    private String driverBannerId;
    private IRatingReviewRepository ratingReviewRepository;
    private ICabBookingRepository cabBookingRepository;
    private Mapper mapper;

    @BeforeEach
    void setUp() {

        rideRequestRepository = mock(IRideRequestRepository.class);
        rideRepository = mock(IRideRepository.class);
        userRepository = mock(IUserRepository.class);
        ratingReviewRepository = mock(IRatingReviewRepository.class);
        mapper = mock(Mapper.class);
        cabBookingRepository = mock(ICabBookingRepository.class);

        emailUtil = mock(IEmailUtil.class);

        rideRequestService = new RideRequestService(rideRequestRepository, rideRepository, userRepository, emailUtil, mapper, ratingReviewRepository, cabBookingRepository);
        bannerId = "B12345678";
        driverBannerId = "B12345679";

        testDriver = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@dal.ca")
                .bannerId(driverBannerId)
                .phoneNumber("1234567890")
                .roles(Set.of(UserRole.DRIVER))
                .build();

        testRider = User.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@dal.ca")
                .bannerId(bannerId)
                .phoneNumber("0987654321")
                .roles(Set.of(UserRole.RIDER))
                .build();

        testRide = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0))
                .availableSeats(3)
                .build();

        rideRequestDto = RideRequestRequest.builder()
                .rideId(1L)
                .message("I would like to join this ride")
                .build();

        rideRequest = RideRequest.builder()
                .id(1L)
                .ride(testRide)
                .rider(testRider)
                .status(RideRequestStatus.PENDING)
                .message("I would like to join this ride")
                .build();

        rideRequestResponse = RideRequestResponse.builder()
                .rideRequestId(1L)
                .rideId(1L)
                .riderBannerId(bannerId)
                .riderName("Jane Smith")
                .status("PENDING")
                .build();
        rideRequestResponse.setMessage("I would like to join this ride");
    }

    @Test
    void createRideRequest_Success() {

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(rideRequest);

        RideRequestResponse result = rideRequestService.createRideRequest(rideRequestDto, bannerId);

        assertNotNull(result);
        assertEquals(1L, result.getRideId());
        assertEquals(bannerId, result.getRiderBannerId());
        assertEquals("Jane Smith", result.getRiderName());
        assertEquals("PENDING", result.getStatus());
        assertEquals("I would like to join this ride", result.getMessage());

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRepository).findById(1L);
        verify(rideRequestRepository).save(any(RideRequest.class));
    }

    @Test
    void createRideRequest_RiderNotFound_ThrowsException() {
        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideRequestService.createRideRequest(rideRequestDto, bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRepository, never()).findById(any());
        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    void createRideRequest_RideNotFound_ThrowsException() {
        RideRequestRequest invalidRequest = RideRequestRequest.builder()
                .rideId(999L)
                .message("I would like to join this ride")
                .build();

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            rideRequestService.createRideRequest(invalidRequest, bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRepository).findById(999L);
        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    void createRideRequest_InActiveRide_ThrowsException() {
        Ride deletedRide = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0))
                .availableSeats(3)
                .isActive(false)
                .build();

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(deletedRide));

        assertThrows(ValidationException.class, () -> {
            rideRequestService.createRideRequest(rideRequestDto, bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRepository).findById(1L);
        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    void createRideRequest_PastRide_ThrowsException() {
        Ride pastRide = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2023, 11, 20, 10, 0))
                .availableSeats(3)
                .isActive(true)
                .build();

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(pastRide));

        assertThrows(ValidationException.class, () -> {
            rideRequestService.createRideRequest(rideRequestDto, bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRepository).findById(1L);
        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    void createRideRequest_DriverRequestingOwnRide_ThrowsException() {

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));

        assertThrows(ValidationException.class, () -> {
            rideRequestService.createRideRequest(rideRequestDto, driverBannerId);
        });

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository).findById(1L);
        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    void createRideRequest_DuplicateRequest_ThrowsException() {

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(testRide));
        when(rideRequestRepository.existsByRideAndRider(testRide, testRider)).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            rideRequestService.createRideRequest(rideRequestDto, bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRepository).findById(1L);
        verify(rideRequestRepository).existsByRideAndRider(testRide, testRider);
        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    void createRideRequest_NoAvailableSeats_ThrowsException() {
        Ride fullRide = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0))
                .availableSeats(0)
                .isActive(true)
                .build();

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(fullRide));
        when(rideRequestRepository.existsByRideAndRider(fullRide, testRider)).thenReturn(false);

        assertThrows(ValidationException.class, () -> {
            rideRequestService.createRideRequest(rideRequestDto, bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRepository).findById(1L);
        verify(rideRequestRepository).existsByRideAndRider(fullRide, testRider);
        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    void getPendingRideRequestsByDriver_Success() {
        RideRequest pendingRequest1 = RideRequest.builder()
                .id(1L)
                .ride(testRide)
                .rider(testRider)
                .status(RideRequestStatus.PENDING)
                .message("I would like to join")
                .build();

        User anotherRider = User.builder()
                .id(3L)
                .name("Bob Johnson")
                .email("bob@dal.ca")
                .bannerId("B00111222")
                .phoneNumber("5551234567")
                .roles(Set.of(UserRole.RIDER))
                .build();

        RideRequest pendingRequest2 = RideRequest.builder()
                .id(2L)
                .ride(testRide)
                .rider(anotherRider)
                .status(RideRequestStatus.PENDING)
                .message("Can I join?")
                .build();

        List<RideRequest> pendingRequests = Arrays.asList(pendingRequest1, pendingRequest2);

        RideRequestResponse response1 = RideRequestResponse.builder()
                .rideRequestId(1L)
                .rideId(1L)
                .riderBannerId(bannerId)
                .riderName("Jane Smith")
                .status("PENDING")
                .build();
        response1.setMessage("I would like to join");

        RideRequestResponse response2 = RideRequestResponse.builder()
                .rideRequestId(2L)
                .rideId(1L)
                .riderBannerId("B00111222")
                .riderName("Bob Johnson")
                .status("PENDING")
                .build();
        response1.setMessage("Can I join?");

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRequestRepository.findByRideDriverAndStatus(testDriver, RideRequestStatus.PENDING))
                .thenReturn(pendingRequests);

        List<RideRequestResponse> result = rideRequestService.getPendingRideRequestsByDriver(driverBannerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("PENDING", result.get(1).getStatus());
        assertEquals(bannerId, result.get(0).getRiderBannerId());
        assertEquals("B00111222", result.get(1).getRiderBannerId());

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRequestRepository).findByRideDriverAndStatus(testDriver, RideRequestStatus.PENDING);
    }

    @Test
    void getPendingRideRequestsByDriver_DriverNotFound() {
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideRequestService.getPendingRideRequestsByDriver(driverBannerId);
        });

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRequestRepository, never()).findByRideDriverAndStatus(any(), any());
    }

    @Test
    void getPendingRideRequestsByDriver_NoPendingRequests() {
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRequestRepository.findByRideDriverAndStatus(testDriver, RideRequestStatus.PENDING))
                .thenReturn(List.of());

        List<RideRequestResponse> result = rideRequestService.getPendingRideRequestsByDriver(driverBannerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRequestRepository).findByRideDriverAndStatus(testDriver, RideRequestStatus.PENDING);
    }

    @Test
    void updateRideRequestStatus_DriverNotFound() {
        UpdateRideRequestStatusRequest request = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(1L)
                .driverBannerId(driverBannerId)
                .status(RideRequestStatus.ACCEPTED)
                .build();

        when(userRepository.findByBannerId(request.getDriverBannerId())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideRequestService.updateRideRequestStatus(request);
        });

        verify(userRepository).findByBannerId(request.getDriverBannerId());
        verify(rideRequestRepository, never()).findByRideDriverAndId(any(), any());
        verify(rideRequestRepository, never()).save(any());
    }

    @Test
    void updateRideRequestStatus_RideRequestNotFound() {
        UpdateRideRequestStatusRequest request = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(1L)
                .driverBannerId(driverBannerId)
                .status(RideRequestStatus.ACCEPTED)
                .build();

        when(userRepository.findByBannerId(request.getDriverBannerId())).thenReturn(Optional.of(testDriver));
        when(rideRequestRepository.findByRideDriverAndId(testDriver, request.getRideRequestId())).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            rideRequestService.updateRideRequestStatus(request);
        });

        verify(userRepository).findByBannerId(request.getDriverBannerId());
        verify(rideRequestRepository).findByRideDriverAndId(testDriver, request.getRideRequestId());
        verify(rideRequestRepository, never()).save(any());
    }

    @Test
    void updateRideRequestStatus_NotInPendingState() {
        UpdateRideRequestStatusRequest request = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(1L)
                .driverBannerId(driverBannerId)
                .status(RideRequestStatus.ACCEPTED)
                .build();

        RideRequest acceptedRequest = RideRequest.builder()
                .id(request.getRideRequestId())
                .ride(testRide)
                .rider(testRider)
                .status(RideRequestStatus.ACCEPTED)
                .message("Already accepted")
                .build();

        when(userRepository.findByBannerId(request.getDriverBannerId())).thenReturn(Optional.of(testDriver));
        when(rideRequestRepository.findByRideDriverAndId(testDriver, request.getRideRequestId())).thenReturn(Optional.of(acceptedRequest));

        assertThrows(ValidationException.class, () -> {
            rideRequestService.updateRideRequestStatus(request);
        });

        verify(userRepository).findByBannerId(request.getDriverBannerId());
        verify(rideRequestRepository).findByRideDriverAndId(testDriver, request.getRideRequestId());
        verify(rideRequestRepository, never()).save(any());
    }

    @Test
    void updateRideRequestStatus_Success() {
        ReflectionTestUtils.setField(rideRequestService, "appName", "TestApp");

        UpdateRideRequestStatusRequest request = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(1L)
                .driverBannerId(driverBannerId)
                .status(RideRequestStatus.ACCEPTED)
                .build();

        RideRequest pendingRequest = RideRequest.builder()
                .id(request.getRideRequestId())
                .ride(testRide)
                .rider(testRider)
                .status(RideRequestStatus.PENDING)
                .message("Please accept me")
                .build();

        when(userRepository.findByBannerId(request.getDriverBannerId())).thenReturn(Optional.of(testDriver));
        when(rideRequestRepository.findByRideDriverAndId(testDriver, request.getRideRequestId())).thenReturn(Optional.of(pendingRequest));
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(pendingRequest);

        boolean result = rideRequestService.updateRideRequestStatus(request);

        assertTrue(result);

        verify(userRepository).findByBannerId(request.getDriverBannerId());
        verify(rideRequestRepository).findByRideDriverAndId(testDriver, request.getRideRequestId());
        verify(rideRequestRepository).save(any(RideRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "ACCEPTED, ACCEPTED, true",
            "REJECTED, REJECTED, false"
    })
    void updateRideRequestStatus_SendsAcceptedNotification(String statusInput, String expectedStatus, boolean isAccepted) {
        RideRequestStatus status = RideRequestStatus.valueOf(statusInput);
        UpdateRideRequestStatusRequest request = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(1L)
                .driverBannerId(driverBannerId)
                .status(status)
                .build();

        RideRequest pendingRequest = RideRequest.builder()
                .id(request.getRideRequestId())
                .ride(testRide)
                .rider(testRider)
                .status(RideRequestStatus.PENDING)
                .message("Please accept me")
                .build();
        ReflectionTestUtils.setField(rideRequestService, "appName", "TestApp");

        when(userRepository.findByBannerId(request.getDriverBannerId())).thenReturn(Optional.of(testDriver));
        when(rideRequestRepository.findByRideDriverAndId(testDriver, request.getRideRequestId())).thenReturn(Optional.of(pendingRequest));
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(pendingRequest);

        rideRequestService.updateRideRequestStatus(request);
        String subject = isAccepted ? "Ride Request Accepted - TestApp" : "Ride Request Rejected - TestApp";

        Map<String, Object> variables = Map.of(
                "riderName", testRider.getName(),
                "driverName", testDriver.getName(),
                "departureLocation", testRide.getDepartureLocation(),
                "destination", testRide.getDestination(),
                "departureTime", testRide.getDepartureDateTime(),
                "isAccepted", isAccepted,
                "appName", "TestApp"
        );

        verify(emailUtil).sendEmail(
                eq(testRider.getEmail()),
                contains(subject),
                eq("ride-request-status-update"),
                eq(variables)
        );
    }

    @Test
    void updateRideRequestStatus_RejectRequest_AddsBackAvailableSeats() {
        ReflectionTestUtils.setField(rideRequestService, "appName", "TestApp");

        UpdateRideRequestStatusRequest request = UpdateRideRequestStatusRequest.builder()
                .rideRequestId(1L)
                .driverBannerId(driverBannerId)
                .status(RideRequestStatus.REJECTED)
                .build();

        Ride rideWithSeats = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0))
                .availableSeats(2)
                .isActive(true)
                .build();

        RideRequest pendingRequest = RideRequest.builder()
                .id(request.getRideRequestId())
                .ride(rideWithSeats)
                .rider(testRider)
                .status(RideRequestStatus.PENDING)
                .message("Please accept me")
                .build();


        when(userRepository.findByBannerId(request.getDriverBannerId())).thenReturn(Optional.of(testDriver));
        when(rideRequestRepository.findByRideDriverAndId(testDriver, request.getRideRequestId())).thenReturn(Optional.of(pendingRequest));
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(pendingRequest);
        when(rideRepository.save(any(Ride.class))).thenReturn(rideWithSeats);

        rideRequestService.updateRideRequestStatus(request);

        verify(rideRepository).save(argThat(ride ->
                ride.getAvailableSeats() == 3
        ));
    }

    @Test
    void createRideRequest_DeductsAvailableSeats() {

        Ride rideWithSeats = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0))
                .availableSeats(3)
                .isActive(true)
                .build();
        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(rideWithSeats));
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(rideRequest);
        when(rideRepository.save(any(Ride.class))).thenReturn(rideWithSeats);

        rideRequestService.createRideRequest(rideRequestDto, bannerId);

        verify(rideRepository).save(argThat(ride ->
                ride.getAvailableSeats() == 2
        ));
    }

    @Test
    void getRideRequestsByRider_RiderNotFound() {
        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideRequestService.getRideRequestsByRider(bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository, never()).findByRiderAndStatusIn(any(), any());
    }

    @Test
    void getRideRequestsByRider_EmptyList() {
        List<RideRequestStatus> statuses = Arrays.asList(
                RideRequestStatus.PENDING,
                RideRequestStatus.ACCEPTED,
                RideRequestStatus.REJECTED
        );

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRequestRepository.findByRiderAndStatusIn(testRider, statuses))
                .thenReturn(List.of());

        List<RideRequestResponse> result = rideRequestService.getRideRequestsByRider(bannerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository).findByRiderAndStatusIn(testRider, statuses);
    }

    @Test
    void getRideRequestsByRider_Success() {
        RideRequest pendingRequest = RideRequest.builder()
                .id(1L)
                .ride(testRide)
                .rider(testRider)
                .status(RideRequestStatus.PENDING)
                .message("Pending request")
                .build();

        Ride anotherRide = Ride.builder()
                .id(2L)
                .driver(testDriver)
                .departureLocation("Truro")
                .destination("Halifax")
                .departureDateTime(LocalDateTime.of(2026, 11, 25, 14, 0))
                .availableSeats(2)
                .isActive(true)
                .build();

        RideRequest acceptedRequest = RideRequest.builder()
                .id(2L)
                .ride(anotherRide)
                .rider(testRider)
                .status(RideRequestStatus.ACCEPTED)
                .message("Accepted request")
                .build();

        Ride thirdRide = Ride.builder()
                .id(3L)
                .driver(testDriver)
                .departureLocation("Sydney")
                .destination("Halifax")
                .departureDateTime(LocalDateTime.of(2026, 11, 22, 9, 0))
                .availableSeats(1)
                .isActive(true)
                .build();

        RideRequest rejectedRequest = RideRequest.builder()
                .id(3L)
                .ride(thirdRide)
                .rider(testRider)
                .status(RideRequestStatus.REJECTED)
                .message("Rejected request")
                .build();

        List<RideRequest> allRequests = Arrays.asList(pendingRequest, acceptedRequest, rejectedRequest);

        RideRequestResponse response1 = RideRequestResponse.builder()
                .rideRequestId(1L)
                .rideId(1L)
                .riderBannerId(bannerId)
                .riderName("Jane Smith")
                .status("PENDING")
                .build();
        response1.setMessage("Pending request");

        RideRequestResponse response2 = RideRequestResponse.builder()
                .rideRequestId(2L)
                .rideId(2L)
                .riderBannerId(bannerId)
                .riderName("Jane Smith")
                .status("ACCEPTED")
                .build();
        response1.setMessage("Accepted request");

        RideRequestResponse response3 = RideRequestResponse.builder()
                .rideRequestId(3L)
                .rideId(3L)
                .riderBannerId(bannerId)
                .riderName("Jane Smith")
                .status("REJECTED")
                .build();
        response1.setMessage("Rejected request");

        List<RideRequestStatus> statuses = Arrays.asList(
                RideRequestStatus.PENDING,
                RideRequestStatus.ACCEPTED,
                RideRequestStatus.REJECTED
        );

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRequestRepository.findByRiderAndStatusIn(testRider, statuses))
                .thenReturn(allRequests);

        List<RideRequestResponse> result = rideRequestService.getRideRequestsByRider(bannerId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("ACCEPTED", result.get(1).getStatus());
        assertEquals("REJECTED", result.get(2).getStatus());

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository).findByRiderAndStatusIn(testRider, statuses);
    }

    @Test
    void getCurrentBookingsForRider_RiderNotFound() {
        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> {
            rideRequestService.getCurrentBookingsForRider(bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository, never()).findCurrentBookingsForRider(any());
    }

    @Test
    void getCurrentBookingsForRider_NoCurrentBooking() {
        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRequestRepository.findCurrentBookingsForRider(testRider))
                .thenReturn(List.of());

        List<RideRequestStatusResponse> result = rideRequestService.getCurrentBookingsForRider(bannerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository).findCurrentBookingsForRider(testRider);
    }

    @Test
    void getCurrentBookingsForRider_Success() {
        Ride ongoingRide = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0))
                .availableSeats(2)
                .status(RideStatus.STARTED)
                .isActive(true)
                .build();
        RideRequest currentBooking = RideRequest.builder()
                .id(1L)
                .ride(ongoingRide)
                .rider(testRider)
                .status(RideRequestStatus.ACCEPTED)
                .message("Current booking")
                .build();

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRequestRepository.findCurrentBookingsForRider(testRider))
                .thenReturn(List.of(currentBooking));

        List<RideRequestStatusResponse> result = rideRequestService.getCurrentBookingsForRider(bannerId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RideRequestStatus.ACCEPTED.name(), result.get(0).getRideRequestStatus());

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository).findCurrentBookingsForRider(testRider);
    }

    @Test
    void getCompletedRidesForRider_RiderNotFound() {
        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideRequestService.getConfirmedRides(bannerId);
        });

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository, never()).findCompletedBookingsForRider(any());
    }

    @Test
    void getCompletedRidesForRider_NoCompletedRides() {
        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRequestRepository.findCompletedBookingsForRider(testRider))
                .thenReturn(List.of());

        List<RideRequestStatusResponse> result = rideRequestService.getConfirmedRides(bannerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository).findCompletedBookingsForRider(testRider);

    }

    @Test
    void getCompletedRidesForRider_Success() {
        Ride completedRide = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2026, 10, 15, 10, 0))
                .availableSeats(2)
                .status(RideStatus.COMPLETED)
                .isActive(true)
                .build();
        RideRequest completedBooking = RideRequest.builder()
                .id(1L)
                .ride(completedRide)
                .rider(testRider)
                .status(RideRequestStatus.ACCEPTED)
                .message("Completed booking")
                .build();

        when(userRepository.findByBannerId(bannerId)).thenReturn(Optional.of(testRider));
        when(rideRequestRepository.findCompletedBookingsForRider(testRider))
                .thenReturn(List.of(completedBooking));

        List<RideRequestStatusResponse> result = rideRequestService.getConfirmedRides(bannerId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RideRequestStatus.ACCEPTED.name(), result.get(0).getRideRequestStatus());

        verify(userRepository).findByBannerId(bannerId);
        verify(rideRequestRepository).findCompletedBookingsForRider(testRider);
    }

}